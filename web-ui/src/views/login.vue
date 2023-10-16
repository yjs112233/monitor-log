<template>
  <div class="login">
    <img class="login-img" src="../assets/login/login-bg.png" alt="" />
    <div class="card">
      <div class="header-title">洺信科技智能日志管理</div>
      <div class="card-item">
        <el-form
          ref="loginForm"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <div class="title">欢迎登录</div>
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              type="text"
              auto-complete="off"
              placeholder="手机号"
            >
              <svg-icon
                slot="prefix"
                icon-class="user"
                class="el-input__icon input-icon"
              />
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              auto-complete="off"
              placeholder="密码"
              @keyup.enter.native="handleLogin"
            >
              <svg-icon
                slot="prefix"
                icon-class="password"
                class="el-input__icon input-icon"
              />
            </el-input>
          </el-form-item>

          <!-- <el-checkbox v-model="loginForm.rememberMe" style="margin:0px 0px 25px 0px;">记住密码</el-checkbox> -->
          <el-form-item style="width: 100%">
            <el-button
              :loading="loading"
              size="medium"
              type="primary"
              class="btn"
              style="width: 100%"
              @click.native.prevent="handleLogin"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登 录 中...</span>
            </el-button>
            <div style="float: right" v-if="register">
              <router-link class="link-type" :to="'/register'"
                >立即注册</router-link
              >
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from "@/api/index";
import { setToken } from "@/utils/auth";
export default {
  name: "Login",
  data() {
    return {
      codeUrl: "",
      year: "", //今年
      loginForm: {
        username: "admin",
        password: "TOURism2021!!",
        rememberMe: false,
        code: "",
        //   uuid: ""
      },
      loginRules: {
        username: [
          { required: true, trigger: "blur", message: "请输入您的账号" },
        ],
        password: [
          { required: true, trigger: "blur", message: "请输入您的密码" },
        ],
        //   code: [{ required: true, trigger: "change", message: "请输入验证码" }]
      },
      loading: false,
      // 验证码开关
      captchaOnOff: true,
      // 注册开关
      register: false,
      redirect: undefined,
    };
  },
  created() {},
  methods: {
    handleLogin() {
      login(this.loginForm).then((res) => {
        setToken(res.data);
        this.$router.replace({ path: "/" });
      });
    },
  },
};
</script>

<style scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.title {
  font-family: PingFang SC;
  font-size: 30px;
  margin: 0px auto 65px auto;
  text-align: center;
  color: #474747;
}

/* .login-content {
    width: 440px;
    height: 402px;
    padding: 20px;
    background-color: #3C8DBC;
    opacity: 0.2;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
} */

.login-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: -1;
}
.card {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.header-title {
  font-family: FZZhengHeiS-DB-GB;
  font-size: 54px;
  font-weight: normal;
  line-height: 54px;
  color: #ffffff;
  margin-bottom: 55px;
}
.card-item {
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  background: #ffffff;
  width: 400px;
  padding: 25px 25px 5px 25px;
  border-radius: 5px;
}

.login-form .el-input {
  height: 38px;
}

.login-form .el-input input {
  height: 38px;
}

.login-form .input-icon {
  height: 39px;
  width: 14px;
  margin-left: 2px;
}

.login-tip {
  font-size: 13px;
  text-align: center;
  color: #bfbfbf;
}

.login-code {
  width: 33%;
  height: 38px;
  float: right;
}

.el-login-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}

.login-code-img {
  height: 38px;
}
.btn {
  background-color: #1175fe;
}
</style>
