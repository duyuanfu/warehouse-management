// pages/index/index.js
const app = getApp();

Page({
  data: {
    userInfo: null
  },

  onShow() {
    if (!app.checkLogin()) return;
    this.setData({
      userInfo: app.globalData.userInfo
    });
  },

  navToGoods() {
    app.globalData.onlyWarning = false;
    wx.switchTab({
      url: '/pages/goods/list'
    });
  },

  navToInboundCreate() {
    wx.navigateTo({ url: '/pages/inbound/create' });
  },

  navToInboundList() {
    wx.navigateTo({ url: '/pages/inbound/list' });
  },

  navToOutboundCreate() {
    wx.navigateTo({ url: '/pages/outbound/create' });
  },

  navToOutboundList() {
    wx.navigateTo({ url: '/pages/outbound/list' });
  },

  navToCheckCreate() {
    wx.navigateTo({ url: '/pages/check/create' });
  },

  navToCheckList() {
    wx.navigateTo({ url: '/pages/check/list' });
  }
});
