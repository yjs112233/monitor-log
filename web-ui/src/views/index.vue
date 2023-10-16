<template>
    <div class="app-container-right-content">
        <div class="search-header">
            <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="68px">
                <el-form-item label="请求参数" prop="operParam" class="mr15">
                    <el-input v-model.trim="queryParams.operParam" placeholder="请输入请求参数" clearable size="small"
                        @keyup.enter.native="handleQuery" />
                </el-form-item>
                <el-form-item label="返回结果" prop="jsonBody" class="mr15">
                    <el-input v-model.trim="queryParams.jsonBody" placeholder="请输入返回结果" clearable size="small"
                        @keyup.enter.native="handleQuery" />
                </el-form-item>
                <el-form-item label="请求类型" prop="operType" class="mr15">
                    <el-select v-model="queryParams.operType" placeholder="请求类型" clearable size="small">
                        <el-option v-for="item in operTypeArr" :key="item.value" :label="item.label"
                            :value="item.value" />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
                    <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
                </el-form-item>
            </el-form>
        </div>

        <div class="body-content">
            <el-table v-loading="loading" :data="postList" empty-text="暂无数据">
                <el-table-column label="用户名" align="center" prop="username" />
                <el-table-column label="请求IP" width="100" align="center" prop="operIp" />
                <el-table-column label="请求地址" align="center" prop="operUrl" />
                <el-table-column label="请求类型" width="100" align="center" prop="operType" />
                <el-table-column label="请求参数" align="center" prop="operParam" show-overflow-tooltip />
                <el-table-column label="返回结果" align="center" prop="jsonBody" show-overflow-tooltip />
                <el-table-column label="返回状态码" width="100" align="center" prop="resCode" />
                <el-table-column label="耗时" width="100" align="center" prop="costTime" />
                <el-table-column label="创建时间" width="150" align="center" prop="createTime" />
            </el-table>

            <!-- <el-pagination v-show="total" class="pagina" :page-size="''" @current-change="getList" background
                layout="prev, pager, next" :total="total">
            </el-pagination> -->
            <el-pagination class="pagina" background :current-page.sync="queryParams.page"
                :page-size.sync="queryParams.size" layout="prev, pager, next" :total="total" @size-change="getList"
                @current-change="getList" />
        </div>

    </div>
</template>
  
<script>
import { listPost } from "@/api/index";

export default {
    name: "index",
    data() {
        return {
            // 遮罩层
            loading: false,
            // 总条数
            total: 0,
            // 岗位表格数据
            postList: [],


            // 查询参数
            queryParams: {
                page: 1,
                size: 10,
                operParam:'',
                jsonBody:'',
            },
            operTypeArr:[
                {label:'GET',value:'GET'},
                {label:'POST',value:'POST'},
                {label:'PUT',value:'PUT'},
                {label:'DELETE',value:'DELETE'},
            ]

        };
    },
    created() {
        this.getList();
    },
    methods: {
        /** 查询岗位列表 */
        getList() {
            this.loading = true;
            listPost(this.queryParams).then(response => {
                this.postList = response.data.list;
                this.total = response.data.count;
                this.loading = false;
            });
        },


        /** 搜索按钮操作 */
        handleQuery() {
            this.queryParams.page = 1;
            this.getList();
        },
        /** 重置按钮操作 */
        resetQuery() {
            this.$refs["queryForm"].resetFields();
            // this.resetForm("queryForm");
            this.handleQuery();
        },



    }
};
</script>

<style scoped>
.app-container-right-content {
    height: 100% !important;
    padding: 15px;
}

.search-header {
    border-radius: 6px;
    margin-bottom: 15px;
    padding: 10px 20px;
    background-color: #fff;
    box-shadow: 0 5px 5px rgba(0, 0, 0, 0.1), 0 0 10px 0 rgba(0, 0, 0, 0.2);
}

.body-content {
    height: 100%;
    border-radius: 6px;
    padding: 20px;
    background-color: #fff;
    box-shadow: 0 5px 5px rgba(0, 0, 0, 0.1), 0 0 10px 0 rgba(0, 0, 0, 0.2);
}

.mb8 {
    margin-bottom: 8px;
}
.mr15{
    margin-right: 15px;
}

.pagina {
    margin-top: 20px;
    text-align: right;
}
.body-content /deep/ .el-table__cell  .cell{
    color: #515a6e;

}
.body-content /deep/ .el-table__header th{
    background-color: #f8f8f9;
}
</style>