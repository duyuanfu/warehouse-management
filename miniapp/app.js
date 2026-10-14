// app.js
App({
  globalData: {
    baseUrl: 'http://127.0.0.1:8080/api',
    userInfo: null,
    token: '',
    onlyWarning: false
  },

  onLaunch() {
    const userInfo = wx.getStorageSync('userInfo');
    const token = wx.getStorageSync('token');
    if (userInfo && token) {
      this.globalData.userInfo = userInfo;
      this.globalData.token = token;
    }
  },

  checkLogin() {
    if (!this.globalData.userInfo) {
      wx.reLaunch({
        url: '/pages/login/login'
      });
      return false;
    }
    return true;
  }
});
