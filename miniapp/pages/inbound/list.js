// pages/inbound/list.js
const request = require('../../utils/request');

Page({
  data: {
    orderList: [],
    pageNo: 1,
    pageSize: 10,
    hasMore: true,
    showDetailModal: false,
    currentDetail: null
  },

  onShow() {
    this.refreshList();
  },

  onPullDownRefresh() {
    this.refreshList().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore) {
      this.loadList(this.data.pageNo + 1);
    }
  },

  refreshList() {
    this.setData({ pageNo: 1, orderList: [], hasMore: true });
    return this.loadList(1);
  },

  async loadList(page) {
    try {
      const res = await request.get('/inbound/page', {
        pageNo: page,
        pageSize: this.data.pageSize
      });
      const records = res.records || [];
      this.setData({
        orderList: page === 1 ? records : this.data.orderList.concat(records),
        pageNo: page,
        hasMore: this.data.orderList.length + records.length < res.total
      });
    } catch (e) {
      console.error(e);
    }
  },

  async viewDetail(e) {
    const id = e.currentTarget.dataset.id;
    try {
      const data = await request.get(`/inbound/${id}`);
      this.setData({
        currentDetail: data,
        showDetailModal: true
      });
    } catch (e) {
      console.error(e);
    }
  },

  closeDetailModal() {
    this.setData({ showDetailModal: false, currentDetail: null });
  },

  stopProp() {}
});
