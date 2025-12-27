<script setup lang="ts">
import type { ColumnDef, RowSelectionState, Table } from '@tanstack/vue-table'
import type { OmsParameter } from '~/api/models'
import { useApi } from '~/api/useApi'

const { OmsParameterApi } = useApi()

const data = ref<OmsParameter[]>([])
const loading = ref(false)
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)

const filters = reactive({
  paramCode: '' as string,
  paramName: '' as string,
  kindCode: '' as string,
  enabledFlag: '' as '' | '1' | '0',
})

const columns: ColumnDef<OmsParameter>[] = [
  {
    header: '参数代码',
    accessorKey: 'paramCode',
  },
  {
    header: '参数名称',
    accessorKey: 'paramName',
  },
  {
    header: '参数类型',
    accessorKey: 'kindCode',
  },
  {
    header: '参数值',
    accessorKey: 'paramValue',
  },
  {
    header: '参数描述',
    accessorKey: 'paramDesc',
  },
  {
    header: '状态',
    accessorKey: 'enabledFlag',
  },
  {
    header: '更新时间',
    accessorKey: 'updateTime',
  },
  {
    header: '操作',
    id: 'actions',
    cell: (info) => {
      return h('div', { class: 'flex gap-2' }, [
        h(resolveComponent('NButton'), {
          label: '编辑',
          btn: 'ghost-primary',
          size: 'sm',
          onClick: () => {
            selectedParameter.value = info.row.original
            isEditDialogOpen.value = true
          },
        }),
      ])
    },
  },
]

const select = ref<RowSelectionState>()

const table = useTemplateRef<Table<OmsParameter>>('table')

const isEditDialogOpen = ref(false)
const selectedParameter = ref<OmsParameter | null>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const result = await OmsParameterApi.omsParameterPageGet({
      operator: 'admin',
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      paramCode: filters.paramCode ? Number(filters.paramCode) : undefined,
      paramName: filters.paramName || undefined,
      enabledFlag: filters.enabledFlag === '' ? undefined : Number(filters.enabledFlag),
    })

    if (result.code === 200) {
      const rows = result.data?.rows || []
      data.value = filters.kindCode
        ? rows.filter(r => (r.kindCode || '').toLowerCase().includes(filters.kindCode.toLowerCase()))
        : rows
      total.value = result.data?.total || 0
    }
  } catch (error) {
    console.error('Failed to fetch parameters:', error)
  } finally {
    loading.value = false
  }
}

const handleEdit = async (row: OmsParameter) => {
  try {
    const result = await OmsParameterApi.omsParameterEditPost({
      addParameterInput: {
        ...row,
        operator: 'admin',
      },
    })
    if (result.code === 200) {
      // Refresh data or show success message
      fetchData()
    }
  } catch (error) {
    console.error('Failed to edit parameter:', error)
  }
}

watch([pageNo, pageSize], () => {
  fetchData()
})

watch(filters, () => {
  // 过滤条件变化时回到第一页
  pageNo.value = 1
  fetchData()
}, { deep: true })

onMounted(() => {
  fetchData()
})
</script>

<template>
  <div class="flex flex-col space-y-4">
    <!-- header -->
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div class="grid w-full gap-2 md:grid-cols-4">
        <NInput
          v-model="filters.paramCode"
          type="number"
          placeholder="参数代码"
        />

        <NInput
          v-model="filters.paramName"
          placeholder="参数名称"
        />

        <NInput
          v-model="filters.kindCode"
          placeholder="参数类型"
        />

        <NSelect
          :items="['全部状态', '启用', '禁用']"
          :model-value="filters.enabledFlag === '' ? '全部状态' : filters.enabledFlag === '1' ? '启用' : '禁用'"
          @update:model-value="filters.enabledFlag = $event === '启用' ? '1' : $event === '禁用' ? '0' : ''"
        />
      </div>

      <div class="flex items-center gap-x-2 sm:ml-auto">
        <NButton
            label="刷新"
            btn="solid-gray"
            leading="i-radix-icons-update"
            class="w-full sm:w-auto sm:shrink-0 active:translate-y-0.5"
            :loading="loading"
            @click="fetchData"
        />
      </div>
    </div>

    <!-- table -->
    <NTable
        ref="table"
        v-model:row-selection="select"
    :loading
        :columns
        :data
        enable-row-selection
        row-id="paramCode"
    >
      <!-- cells -->
      <template #enabledFlag-cell="{ cell }">
        <NBadge
            :una="{
            badgeDefaultVariant: cell.row.original.enabledFlag === 1
              ? 'badge-soft-success' : 'badge-soft-error' }"
            class="capitalize"
            :label="cell.row.original.enabledFlag === 1 ? '启用' : '禁用'"
        />
      </template>
      <!-- end cell -->
    </NTable>

    <!-- footer -->
    <div
        class="flex items-center justify-between px-2"
    >
      <div
          class="hidden text-sm text-muted sm:block"
      >
        已选择 {{ table?.getFilteredSelectedRowModel().rows.length.toLocaleString() }} / {{ total.toLocaleString() }} 条记录
      </div>
      <div class="flex items-center space-x-6 lg:space-x-8">
        <div
            class="hidden items-center justify-center text-sm font-medium sm:flex space-x-2"
        >
          <span class="text-nowrap">
            每页行数
          </span>

          <NSelect
              :items="[10, 20, 30, 40, 50]"
              :_select-trigger="{
              class: 'w-15',
            }"
              :model-value="pageSize"
              @update:model-value="pageSize = $event as unknown as number"
          />
        </div>

        <div
            class="flex items-center justify-center text-sm font-medium"
        >
          第 {{ pageNo }} 页，共 {{ Math.ceil(total / pageSize) }} 页
        </div>

        <NPagination
            :page="pageNo"
            :total="total"
            :show-list-item="false"
            :items-per-page="pageSize"
            @update:page="pageNo = $event"
        />
      </div>
    </div>

    <ParameterEditDialog
      v-model:open="isEditDialogOpen"
      :parameter="selectedParameter"
      @save="handleEdit"
    />
  </div>
</template>
