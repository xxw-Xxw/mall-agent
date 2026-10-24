<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable style="width: 220px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5" v-if="activeTab === 'pending'">
        <el-button type="success" plain icon="Van" :disabled="!ids.length" @click="handleBatchShip" v-hasPermi="['system:orderProcess:ship']">批量发货</el-button>
      </el-col>
      <right-toolbar v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="待发货" name="pending" />
      <el-tab-pane label="已发货" name="shipped" />
    </el-tabs>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange" @expand-change="handleExpand">
      <el-table-column v-if="activeTab === 'pending'" type="selection" width="50" align="center" />
      <el-table-column type="expand">
        <template #default="scope">
          <div class="expand-box">
            <el-table v-if="scope.row.items" :data="scope.row.items" size="small">
              <el-table-column label="商品ID" prop="goodsId" width="100" align="center" />
              <el-table-column label="商品名称" prop="goodsName" min-width="220" />
              <el-table-column label="单价" width="110" align="right">
                <template #default="s">¥{{ s.row.price }}</template>
              </el-table-column>
              <el-table-column label="数量" prop="quantity" width="90" align="center" />
              <el-table-column label="小计" width="110" align="right">
                <template #default="s">¥{{ (Number(s.row.price) * Number(s.row.quantity)).toFixed(2) }}</template>
              </el-table-column>
            </el-table>
            <div v-else class="expand-loading">加载中...</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="订单号" prop="orderNo" min-width="190" />
      <el-table-column label="会员" prop="memberName" width="120" />
      <el-table-column label="商品件数" prop="itemCount" width="90" align="center" />
      <el-table-column label="订单金额" width="120" align="right">
        <template #default="scope">
          <span class="amount">¥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注" prop="remark" min-width="120" show-overflow-tooltip>
        <template #default="scope">
          <span v-if="scope.row.remark">{{ scope.row.remark }}</span>
          <span v-else class="no-remark">-</span>
        </template>
      </el-table-column>
      <el-table-column v-if="activeTab === 'pending'" label="下单时间" align="center" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <template v-else>
        <el-table-column label="发货人" prop="updateBy" width="100" align="center" />
        <el-table-column label="发货时间" align="center" width="170">
          <template #default="scope">
            <span>{{ parseTime(scope.row.updateTime) }}</span>
          </template>
        </el-table-column>
      </template>
      <el-table-column v-if="activeTab === 'pending'" label="操作" align="center" width="110" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="success" icon="Van" @click="handleShip(scope.row)" v-hasPermi="['system:orderProcess:ship']">发货</el-button>
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

<script setup>
import { onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listPending, listShipped, shipOrder, shipOrders, getOrderItems } from '@/api/system/orderProcess'
import { parseTime } from '@/utils/ruoyi'

const { proxy } = getCurrentInstance()

const activeTab = ref('pending')
const orderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const ids = ref([])

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: null }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  const api = activeTab.value === 'pending' ? listPending : listShipped
  api(queryParams.value).then((response) => {
    orderList.value = response.rows.map((row) => ({ ...row, items: null }))
    total.value = response.total
    loading.value = false
  })
}

function handleTabChange() {
  ids.value = []
  queryParams.value.pageNum = 1
  getList()
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleSelectionChange(selection) {
  ids.value = selection.map((item) => item.orderId)
}

function handleExpand(row, expandedRows) {
  if (!row.items && expandedRows.includes(row)) {
    getOrderItems(row.orderId).then((res) => {
      row.items = res.data
    })
  }
}

function handleShip(row) {
  ElMessageBox.confirm('是否确认发货订单 "' + row.orderNo + '"？', '发货确认', {
    confirmButtonText: '确认发货',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return shipOrder(row.orderId).then(() => {
      ElMessage.success('订单已发货')
      getList()
    })
  }).catch(() => {})
}

function handleBatchShip() {
  const count = ids.value.length
  ElMessageBox.confirm('是否确认发货选中的 ' + count + ' 个订单？', '批量发货', {
    confirmButtonText: '确认发货',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return shipOrders(ids.value.join(',')).then((res) => {
      const skipped = res.skipped > 0 ? '，跳过 ' + res.skipped + ' 单（状态已变化）' : ''
      ElMessage.success('成功发货 ' + res.shipped + ' 单' + skipped)
      getList()
    })
  }).catch(() => {})
}

onMounted(getList)
</script>

<style scoped>
.expand-box {
  padding: 8px 16px 12px 60px;
  background: #fafbfc;
}

.expand-loading {
  color: #909399;
  font-size: 13px;
  padding: 10px 0;
}

.amount {
  color: #ff6b35;
  font-weight: 700;
}

.no-remark {
  color: #c0c4cc;
}
</style>
