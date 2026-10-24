<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="goodsName">
        <el-input v-model="queryParams.goodsName" placeholder="请输入商品名称" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-alert title="按商品维度统计的收藏热度：收藏人数为收藏过该商品的用户数" type="info" :closable="false" class="mb8" />

    <el-table v-loading="loading" :data="statsList" @sort-change="handleSortChange" :default-sort="{ prop: 'favorite_count', order: 'descending' }">
      <el-table-column label="排名" width="80" align="center">
        <template #default="scope">
          <span :class="['rank', 'rank-' + (scope.$index + 1)]" v-if="scope.$index < 3 && queryParams.orderByColumn === 'favorite_count' && queryParams.isAsc === 'desc'">{{ scope.$index + 1 }}</span>
          <span v-else>{{ scope.$index + 1 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="商品" min-width="300">
        <template #default="scope">
          <div class="goods-cell">
            <el-image v-if="scope.row.image_url" :src="scope.row.image_url" fit="cover" class="goods-thumb" :preview-src-list="[scope.row.image_url]" preview-teleported />
            <div class="goods-info">
              <div class="goods-name">{{ scope.row.goods_name || ('商品 ' + scope.row.goods_id) }}</div>
              <div class="goods-id">ID: {{ scope.row.goods_id }}</div>
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="单价" prop="price" width="130" align="right" sortable="custom">
        <template #default="scope">
          <span class="amount">¥{{ scope.row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="收藏人数" prop="favorite_count" width="130" align="center" sortable="custom">
        <template #default="scope">
          <span class="num">{{ scope.row.favorite_count }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最近收藏时间" prop="latest_time" width="170" align="center" sortable="custom">
        <template #default="scope">
          <span>{{ parseTime(scope.row.latest_time) }}</span>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="Favorite">
import { statsFavorite } from '@/api/system/favorite'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()

const statsList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)

const data = reactive({
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    goodsName: null,
    orderByColumn: 'favorite_count',
    isAsc: 'desc'
  }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  statsFavorite(queryParams.value).then((response) => {
    statsList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  queryParams.value.orderByColumn = 'favorite_count'
  queryParams.value.isAsc = 'desc'
  handleQuery()
}

function handleSortChange(column) {
  if (column.order) {
    queryParams.value.orderByColumn = column.prop
    queryParams.value.isAsc = column.order === 'ascending' ? 'asc' : 'desc'
  } else {
    queryParams.value.orderByColumn = 'favorite_count'
    queryParams.value.isAsc = 'desc'
  }
  queryParams.value.pageNum = 1
  getList()
}

getList()
</script>

<style scoped>
.goods-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.goods-thumb {
  width: 46px;
  height: 46px;
  border-radius: 6px;
  flex-shrink: 0;
}

.goods-info {
  min-width: 0;
}

.goods-name {
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-id {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.amount {
  color: #ff6b35;
  font-weight: 700;
}

.num {
  font-weight: 700;
  color: #303133;
}

.rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border-radius: 50%;
  color: #fff;
  font-weight: 700;
}

.rank-1 {
  background: linear-gradient(135deg, #ffb199, #ff6034);
}

.rank-2 {
  background: linear-gradient(135deg, #c0c4cc, #909399);
}

.rank-3 {
  background: linear-gradient(135deg, #e6b27a, #c88f4a);
}

.mb8 {
  margin-bottom: 8px;
}
</style>
