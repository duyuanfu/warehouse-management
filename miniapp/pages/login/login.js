// pages/login/login.js
const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    account: '',
    password: ''
  },

  onLoad() {},

  onInputAccount(e) {
    this.setData({ account: e.detail.value });
  },

  onInputPassword(e) {
    this.setData({ password: e.detail.value });
  },

  async handleLogin() {
    if (!this.data.account.trim()) {
      wx.showToast({ title: '请输入员工账号', icon: 'none' });
      return;
    }
    if (!this.data.password.trim()) {
      wx.showToast({ title: '请输入登录密码', icon: 'none' });
      return;
    }

    try {
      const res = await request.post('/auth/login', {
        account: this.data.account.trim(),
        password: this.data.password.trim()
      });

      wx.setStorageSync('userInfo', res);
      wx.setStorageSync('token', res.token);
      app.globalData.userInfo = res;
      app.globalData.token = res.token;

      wx.showToast({
        title: '登录成功',
        icon: 'success',
        duration: 1000
      });

      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }, 1000);
    } catch (err) {
      console.error('登录失败', err);
    }
  }
});
