// pages/check/create.js
const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    warehouseList: [],
    warehouseIndex: -1,
    goodsList: [],
    remark: ''
  },

  async onLoad() {
    await this.loadWarehouses();
  },

  async loadWarehouses() {
    try {
      const list = await request.get('/warehouse/list');
      this.setData({ warehouseList: list });
    } catch (e) {
      console.error(e);
    }
  },

  async onWarehouseChange(e) {
    const idx = parseInt(e.detail.value);
    const wh = this.data.warehouseList[idx];
    this.setData({ warehouseIndex: idx });

    if (wh) {
      try {
        const goods = await request.get('/goods/by-warehouse', { warehouseId: wh.id });
        // 初始化盘点项，实际库存默认等于当前系统库存，diff = 0
        const formatted = goods.map(g => ({
          ...g,
          actualStock: g.stockQuantity,
          diff: 0,
          remark: ''
        }));
        this.setData({ goodsList: formatted });
      } catch (err) {
        console.error(err);
      }
    }
  },

  onInputRemark(e) {
    this.setData({ remark: e.detail.value });
  },

  onActualStockInput(e) {
    const idx = e.currentTarget.dataset.index;
    const val = e.detail.value;
    const goodsList = this.data.goodsList;
    const item = goodsList[idx];

    const actual = val === '' ? 0 : parseInt(val);
    item.actualStock = val;
    item.diff = (isNaN(actual) ? 0 : actual) - item.stockQuantity;

    this.setData({ goodsList });
  },

  onItemRemarkInput(e) {
    const idx = e.currentTarget.dataset.index;
    const goodsList = this.data.goodsList;
    goodsList[idx].remark = e.detail.value;
    this.setData({ goodsList });
  },

  async handleSubmitCheck() {
    if (this.data.warehouseIndex === -1) {
      wx.showToast({ title: '请选择盘点仓库', icon: 'none' });
      return;
    }
    if (this.data.goodsList.length === 0) {
      wx.showToast({ title: '当前仓库暂无货品', icon: 'none' });
      return;
    }

    const warehouse = this.data.warehouseList[this.data.warehouseIndex];

    const postItems = [];
    for (let i = 0; i < this.data.goodsList.length; i++) {
      const g = this.data.goodsList[i];
      const actual = parseInt(g.actualStock);
      if (isNaN(actual) || actual < 0) {
        wx.showToast({ title: `[${g.name}] 实际库存不能小于0`, icon: 'none' });
        return;
      }
      postItems.push({
        goodsId: g.id,
        actualStock: actual,
        remark: g.remark || (g.diff === 0 ? '账实相符' : (g.diff > 0 ? '盘盈' : '盘亏损耗'))
      });
    }

    const payload = {
      warehouseId: warehouse.id,
      employeeId: app.globalData.userInfo ? app.globalData.userInfo.id : 1,
      remark: this.data.remark || '仓库库存实物核对盘点',
      items: postItems
    };

    try {
      await request.post('/check/create', payload);
      wx.showToast({ title: '盘点记录已保存', icon: 'success' });
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/check/list' });
      }, 1000);
    } catch (e) {
      console.error(e);
    }
  }
});
