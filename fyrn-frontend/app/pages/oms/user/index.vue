<script setup lang="ts">
import type { ColumnDef, RowSelectionState, Table } from '@tanstack/vue-table'
import type { UserInfoVo, UserCreateInput, UserUpdateInput } from '~/api/models'
import { useApi } from '~/api/useApi'
import { formatToYMD } from "~/utils/date";
import UserEditDialog from "~/components/oms/UserEditDialog.vue";

const { OmsUserControllerApi } = useApi()

const data = ref<UserInfoVo[]>([])
const loading = ref(false)
const total = ref(0)
const pageNo = ref(1)
const pageSize = ref(10)

const filters = reactive({
  username: '',
  email: '',
  status: '' as '' | '0' | '1' | '2',
})

const columns: ColumnDef<UserInfoVo>[] = [
  {
    header: '用户名',
    accessorKey: 'username',
  },
  {
    header: '昵称',
    accessorKey: 'nickname',
  },
  {
    header: '邮箱',
    accessorKey: 'email',
  },
  {
    header: '手机号',
    accessorKey: 'phone',
  },
  {
    header: '状态',
    accessorKey: 'status',
  },
  {
    header: '最后登录时间',
    accessorKey: 'lastLoginTime',
    cell: (info) => info.getValue() ? formatToYMD(info.getValue() as string) : '-'
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
          leading: 'i-lucide-edit',
          onClick: () => {
            selectedUser.value = info.row.original
            isEditDialogOpen.value = true
          },
        }),
        h(resolveComponent('NAlertDialog'), {
          title: '确认删除',
          description: `确定要删除用户 ${info.row.original.username} 吗？此操作将永久删除该用户。`,
          _alertDialogAction: {
            label: '确定',
            btn: 'solid-error',
            onClick: () => handleDelete(info.row.original),
          },
          _alertDialogCancel: {
            label: '取消',
          },
        }, {
          trigger: () => h(resolveComponent('NButton'), {
            label: '删除',
            btn: 'ghost-error',
            size: 'sm',
            leading: 'i-lucide-trash-2',
          })
        }),
      ])
    },
  },
]

const select = ref<RowSelectionState>()
const table = useTemplateRef<Table<UserInfoVo>>('table')

const isEditDialogOpen = ref(false)
const selectedUser = ref<UserInfoVo | null>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const result = await OmsUserControllerApi.omsUserListGet({
      pageNo: pageNo.value,
      pageSize: pageSize.value,
      username: filters.username || undefined,
      email: filters.email || undefined,
      status: filters.status === '' ? undefined : Number(filters.status),
    })

    if (result.code === 200) {
      data.value = result.data?.rows || []
      total.value = result.data?.total || 0
    }
  } catch (error) {
    console.error('Failed to fetch users:', error)
  } finally {
    loading.value = false
  }
}

const handleSave = async (payload: UserCreateInput | UserUpdateInput) => {
  try {
    let result
    if (selectedUser.value) {
      result = await OmsUserControllerApi.omsUserUpdatePost({
        userUpdateInput: payload as UserUpdateInput
      })
    } else {
      result = await OmsUserControllerApi.omsUserCreatePost({
        userCreateInput: payload as UserCreateInput
      })
    }

    if (result.code === 200) {
      fetchData()
    } else {
      console.error('Save failed:', result.msg)
    }
  } catch (error) {
    console.error('Failed to save user:', error)
  }
}

const handleDelete = async (user: UserInfoVo) => {
  try {
    const result = await OmsUserControllerApi.omsUserDeleteUserIdPost({
      userId: user.userId!
    })
    if (result.code === 200) {
      fetchData()
    }
  } catch (error) {
    console.error('Failed to delete user:', error)
  }
}

onMounted(() => {
  fetchData()
})

watch([pageNo, pageSize], () => {
  fetchData()
})

watch(filters, () => {
  pageNo.value = 1
  fetchData()
}, { deep: true })
</script>

<template>
  <div class="flex flex-col space-y-4 p-4">
    <div class="flex items-center justify-between">
      <h2 class="text-lg font-semibold">用户管理</h2>
      <NButton label="新增用户" btn="primary" leading="i-radix-icons-plus" @click="selectedUser = null; isEditDialogOpen = true" />
    </div>

    <!-- filters -->
    <div class="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
      <div class="grid w-full gap-2 md:grid-cols-4">
        <NInput v-model="filters.username" placeholder="用户名" />
        <NInput v-model="filters.email" placeholder="邮箱" />
        <NSelect
          :items="['全部状态', '正常', '禁用', '锁定']"
          :model-value="filters.status === '' ? '全部状态' : (filters.status === '1' ? '正常' : (filters.status === '0' ? '禁用' : '锁定'))"
          @update:model-value="val => {
            if (val === '全部状态') filters.status = ''
            else if (val === '正常') filters.status = '1'
            else if (val === '禁用') filters.status = '0'
            else filters.status = '2'
          }"
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
      :loading="loading"
      :columns="columns"
      :data="data"
      enable-row-selection
      row-id="userId"
    >
      <template #status-cell="{ cell }">
        <NBadge
          :una="{
            badgeDefaultVariant: cell.row.original.status === 1
              ? 'badge-soft-success' : (cell.row.original.status === 0 ? 'badge-soft-error' : 'badge-soft-warning')
          }"
          class="capitalize"
          :label="cell.row.original.status === 1 ? '正常' : (cell.row.original.status === 0 ? '禁用' : '锁定')"
        />
      </template>
    </NTable>

    <!-- footer -->
    <div class="flex items-center justify-between px-2">
      <div class="hidden text-sm text-muted sm:block">
        已选择 {{ table?.getFilteredSelectedRowModel().rows.length.toLocaleString() }} / {{ total.toLocaleString() }} 条记录
      </div>
      <div class="flex items-center space-x-6 lg:space-x-8">
        <div class="hidden items-center justify-center text-sm font-medium sm:flex space-x-2">
          <span class="text-nowrap">每页行数</span>
          <NSelect
            :items="[10, 20, 30, 40, 50]"
            :_select-trigger="{ class: 'w-15' }"
            :model-value="pageSize"
            @update:model-value="pageSize = $event as unknown as number"
          />
        </div>
        <div class="flex items-center justify-center text-sm font-medium">
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

    <UserEditDialog
      v-model:open="isEditDialogOpen"
      :user="selectedUser"
      @save="handleSave"
    />
  </div>
</template>
