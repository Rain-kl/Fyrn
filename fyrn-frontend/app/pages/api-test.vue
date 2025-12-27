<script setup lang="ts">
import {useApi} from "~/api/useApi";

const { mmsNovel } = useApi()

const novels = ref<any[]>([])
const loading = ref(false)

const fetchNovels = async () => {
  loading.value = true
  try {
    // 示例：调用分页查询接口
    const result = await mmsNovel.mmsPageGet({
      operator: 'admin',
      pageNo: 1,
      pageSize: 10
    })
    
    if (result.code === 200) {
      novels.value = result.data?.list || []
    }
  } catch (error) {
    console.error('Failed to fetch novels:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchNovels()
})
</script>

<template>
  <div class="p-4">
    <h1 class="text-2xl font-bold mb-4">API 调用示例</h1>
    
    <div v-if="loading" class="py-4">
      加载中...
    </div>
    
    <div v-else>
      <ul class="space-y-2">
        <li v-for="novel in novels" :key="novel.id" class="p-2 border rounded">
          {{ novel.title }} - {{ novel.author }}
        </li>
      </ul>
      
      <div v-if="novels.length === 0" class="text-gray-500">
        暂无数据
      </div>
    </div>
  </div>
</template>
