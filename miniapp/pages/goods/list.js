// pages/goods/list.js
const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    userInfo: null,
    isSupervisor: false,
    keyword: '',
    onlyWarning: false,
    goodsList: [],
    pageNo: 1,
    pageSize: 20,
    hasMore: true
  },

  onShow() {
    const userInfo = app.globalData.userInfo || {};
    this.setData({
      userInfo: userInfo,
      isSupervisor: userInfo.role === 'SUPERVISOR'
    });

    if (app.globalData.onlyWarning !== undefined && app.globalData.onlyWarning !== null) {
      this.setData({ onlyWarning: app.globalData.onlyWarning });
      app.globalData.onlyWarning = false;
    }
    this.refreshList();
  },

  onPullDownRefresh() {
    this.refreshList().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.loadGoodsList(this.data.pageNo + 1);
    }
  },

  refreshList() {
    this.setData({ pageNo: 1, goodsList: [], hasMore: true });
    return this.loadGoodsList(1);
  },

  async loadGoodsList(page) {
    try {
      const res = await request.get('/goods/page', {
        pageNo: page,
        pageSize: this.data.pageSize,
        keyword: this.data.keyword,
        onlyWarning: this.data.onlyWarning
      });

      const records = res.records || [];
      this.setData({
        goodsList: page === 1 ? records : this.data.goodsList.concat(records),
        pageNo: page,
        hasMore: this.data.goodsList.length + records.length < res.total
      });
    } catch (e) {
      console.error(e);
    }
  },

  onInputKeyword(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearchConfirm() {
    this.refreshList();
  },

  clearSearch() {
    this.setData({ keyword: '' });
    this.refreshList();
  },

  switchFilter(e) {
    const onlyWarning = e.currentTarget.dataset.warning === 'true';
    if (this.data.onlyWarning !== onlyWarning) {
      this.setData({ onlyWarning });
      this.refreshList();
    }
  },

  navToCreate() {
    if (!this.data.isSupervisor) {
      wx.showToast({ title: '仅主管可新增货品', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: '/pages/goods/edit' });
  },

  navToEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: `/pages/goods/edit?id=${id}&readonly=${!this.data.isSupervisor}` });
  }
});
