<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="用户ID" prop="userId">
        <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable style="width: 140px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="订单状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
          <el-option v-for="(label, key) in statusText" :key="key" :label="label" :value="key" />
        </el-select>
      </el-form-item>
      <el-form-item label="下单时间">
        <el-date-picker
          v-model="daterangeCreateTime"
          style="width: 240px"
          value-format="YYYY-MM-DD"
          type="daterange"
          range-separator="-"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
        ></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="orderList" @expand-change="handleExpand">
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
      <el-table-column label="用户ID" prop="userId" width="90" align="center" />
      <el-table-column label="总金额" width="120" align="right">
        <template #default="scope">
          <span class="amount">¥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="订单状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="statusType[scope.row.status]">{{ statusText[scope.row.status] || '未知' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" align="center" width="170">
        <template #default="scope">
          <span>{{ parseTime(scope.row.createTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
        <template #default="scope">
          <el-button link type="primary" icon="View" @click="handleDetail(scope.row)" v-hasPermi="['system:order:query']">详情</el-button>
          <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)" v-hasPermi="['system:order:remove']">删除</el-button>
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

    <!-- 订单详情 -->
    <el-dialog title="订单详情" v-model="detailOpen" width="680px" append-to-body>
      <el-descriptions :column="2" border size="default">
        <el-descriptions-item label="订单号">{{ detailForm.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ detailForm.userId }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span class="amount">¥{{ detailForm.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ parseTime(detailForm.createTime) }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-section">
        <span class="section-label">订单状态</span>
        <el-radio-group v-model="detailForm.status" :disabled="detailForm.status === '4'">
          <el-radio value="0">待付款</el-radio>
          <el-radio value="1">待发货</el-radio>
          <el-radio value="2">已发货</el-radio>
          <el-radio value="3">已完成</el-radio>
          <el-radio value="4" disabled>已取消</el-radio>
        </el-radio-group>
        <span v-if="detailForm.status === '4'" class="cancel-tip">订单已取消，状态不可修改</span>
      </div>

      <div class="detail-section">
        <span class="section-label">商品明细</span>
        <el-table :data="detailItems" size="small" border>
          <el-table-column label="商品名称" prop="goodsName" min-width="200" />
          <el-table-column label="单价" width="100" align="right">
            <template #default="s">¥{{ s.row.price }}</template>
          </el-table-column>
          <el-table-column label="数量" prop="quantity" width="80" align="center" />
          <el-table-column label="小计" width="100" align="right">
            <template #default="s">¥{{ (Number(s.row.price) * Number(s.row.quantity)).toFixed(2) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailOpen = false">取 消</el-button>
        <el-button type="primary" :disabled="detailForm.status === '4'" @click="submitStatus" v-hasPermi="['system:order:edit']">保存状态</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listOrder, getOrder, updateOrder, delOrder } from '@/api/system/order'
import { listItem } from '@/api/system/item'
import { addDateRange, parseTime } from '@/utils/ruoyi'

const route = useRoute()
const { proxy } = getCurrentInstance()

const orderList = ref([])
const loading = ref(true)
const showSearch = ref(true)
const total = ref(0)
const detailOpen = ref(false)
const detailItems = ref([])
const detailForm = ref({})
const daterangeCreateTime = ref([])

const statusText = { '0': '待付款', '1': '待发货', '2': '已发货', '3': '已完成', '4': '已取消' }
const statusType = { '0': 'info', '1': 'warning', '2': 'primary', '3': 'success', '4': 'danger' }

const data = reactive({
  queryParams: { pageNum: 1, pageSize: 10, orderNo: null, userId: null, status: null }
})

const { queryParams } = toRefs(data)

function getList() {
  loading.value = true
  listOrder(proxy.addDateRange(queryParams.value, daterangeCreateTime.value)).then((response) => {
    orderList.value = response.rows.map((row) => ({ ...row, items: null }))
    total.value = response.total
    loading.value = false
  })
}

function handleQuery() {
  queryParams.value.pageNum = 1
  getList()
}

function resetQuery() {
  daterangeCreateTime.value = []
  proxy.resetForm('queryRef')
  handleQuery()
}

function handleExpand(row, expandedRows) {
  if (!row.items && expandedRows.includes(row)) {
    listItem({ orderId: row.orderId }).then((res) => {
      row.items = res.rows
    })
  }
}

function handleDetail(row) {
  getOrder(row.orderId).then((res) => {
    detailForm.value = res.data
    detailOpen.value = true
  })
  listItem({ orderId: row.orderId }).then((res) => {
    detailItems.value = res.rows
  })
}

function submitStatus() {
  updateOrder(detailForm.value).then(() => {
    ElMessage.success('订单状态已更新')
    detailOpen.value = false
    getList()
  })
}

function handleDelete(row) {
  ElMessageBox.confirm('是否确认删除订单 "' + row.orderNo + '"？删除后不可恢复', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    return delOrder(row.orderId).then(() => {
      ElMessage.success('删除成功')
      getList()
    })
  }).catch(() => {})
}

onMounted(() => {
  if (route.query.status !== undefined && route.query.status !== null) {
    queryParams.value.status = String(route.query.status)
  }
  getList()
})
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

.detail-section {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.section-label {
  font-weight: 600;
  color: #303133;
}

.cancel-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 10px;
}
</style>
