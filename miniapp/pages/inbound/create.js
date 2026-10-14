// pages/inbound/create.js
const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    supplierList: [],
    supplierIndex: -1,
    warehouseList: [],
    warehouseIndex: -1,
    availableGoods: [],
    remark: '',
    items: [
      { goodsIndex: -1, goodsId: null, quantity: '', price: '', version: 0 }
    ]
  },

  async onLoad() {
    await Promise.all([this.loadSuppliers(), this.loadWarehouses()]);
  },

  async loadSuppliers() {
    try {
      const list = await request.get('/partner/suppliers');
      this.setData({ supplierList: list });
    } catch (e) {
      console.error(e);
    }
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
    this.setData({
      warehouseIndex: idx,
      // 切换仓库时重置明细项
      items: [{ goodsIndex: -1, goodsId: null, quantity: '', price: '', version: 0 }]
    });

    if (wh) {
      // 获取该仓库下的货品
      try {
        const goodsList = await request.get('/goods/by-warehouse', { warehouseId: wh.id });
        this.setData({ availableGoods: goodsList });
      } catch (err) {
        console.error(err);
      }
    }
  },

  onSupplierChange(e) {
    this.setData({ supplierIndex: parseInt(e.detail.value) });
  },

  onInputRemark(e) {
    this.setData({ remark: e.detail.value });
  },

  addItem() {
    if (this.data.warehouseIndex === -1) {
      wx.showToast({ title: '请先选择接收仓库', icon: 'none' });
      return;
    }
    const items = this.data.items;
    items.push({ goodsIndex: -1, goodsId: null, quantity: '', price: '', version: 0 });
    this.setData({ items });
  },

  removeItem(e) {
    const index = e.currentTarget.dataset.index;
    const items = this.data.items;
    items.splice(index, 1);
    this.setData({ items });
  },

  onItemGoodsChange(e) {
    const itemIndex = e.currentTarget.dataset.index;
    const goodsIdx = parseInt(e.detail.value);
    const goods = this.data.availableGoods[goodsIdx];

    const items = this.data.items;
    items[itemIndex].goodsIndex = goodsIdx;
    items[itemIndex].goodsId = goods.id;
    items[itemIndex].price = goods.price || 0;
    items[itemIndex].version = goods.version;
    this.setData({ items });
  },

  onItemQuantity(e) {
    const index = e.currentTarget.dataset.index;
    const items = this.data.items;
    items[index].quantity = e.detail.value;
    this.setData({ items });
  },

  onItemPrice(e) {
    const index = e.currentTarget.dataset.index;
    const items = this.data.items;
    items[index].price = e.detail.value;
    this.setData({ items });
  },

  async handleSubmitInbound() {
    if (this.data.supplierIndex === -1) {
      wx.showToast({ title: '请选择供应商', icon: 'none' });
      return;
    }
    if (this.data.warehouseIndex === -1) {
      wx.showToast({ title: '请选择接收仓库', icon: 'none' });
      return;
    }

    const supplier = this.data.supplierList[this.data.supplierIndex];
    const warehouse = this.data.warehouseList[this.data.warehouseIndex];

    const postItems = [];
    for (let i = 0; i < this.data.items.length; i++) {
      const it = this.data.items[i];
      if (!it.goodsId) {
        wx.showToast({ title: `请选择第${i + 1}项的货品`, icon: 'none' });
        return;
      }
      const qty = parseInt(it.quantity);
      if (!qty || qty <= 0) {
        wx.showToast({ title: `第${i + 1}项数量必须大于0`, icon: 'none' });
        return;
      }
      postItems.push({
        goodsId: it.goodsId,
        quantity: qty,
        price: parseFloat(it.price) || 0.00,
        version: it.version
      });
    }

    const payload = {
      partnerId: supplier.id,
      warehouseId: warehouse.id,
      employeeId: app.globalData.userInfo ? app.globalData.userInfo.id : 1,
      remark: this.data.remark,
      items: postItems
    };

    try {
      await request.post('/inbound/create', payload);
      wx.showToast({ title: '入库成功', icon: 'success' });
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/inbound/list' });
      }, 1000);
    } catch (e) {
      console.error(e);
    }
  }
});
