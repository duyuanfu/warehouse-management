// pages/profile/profile.js
const app = getApp();

Page({
  data: {
    userInfo: null
  },

  onShow() {
    this.setData({
      userInfo: app.globalData.userInfo
    });
  },

  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '是否退出当前员工账号？',
      success(res) {
        if (res.confirm) {
          wx.removeStorageSync('userInfo');
          wx.removeStorageSync('token');
          app.globalData.userInfo = null;
          app.globalData.token = '';
          wx.reLaunch({
            url: '/pages/login/login'
          });
        }
      }
    });
  }
});
