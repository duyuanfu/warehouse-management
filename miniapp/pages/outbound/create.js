// pages/outbound/create.js
const request = require('../../utils/request');
const app = getApp();

Page({
  data: {
    customerList: [],
    customerIndex: -1,
    warehouseList: [],
    warehouseIndex: -1,
    availableGoods: [],
    remark: '',
    items: [
      { goodsIndex: -1, goodsId: null, quantity: '', price: '', version: 0 }
    ]
  },

  async onLoad() {
    await Promise.all([this.loadCustomers(), this.loadWarehouses()]);
  },

  async loadCustomers() {
    try {
      const list = await request.get('/partner/customers');
      this.setData({ customerList: list });
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
      items: [{ goodsIndex: -1, goodsId: null, quantity: '', price: '', version: 0 }]
    });

    if (wh) {
      try {
        const goodsList = await request.get('/goods/by-warehouse', { warehouseId: wh.id });
        this.setData({ availableGoods: goodsList });
      } catch (err) {
        console.error(err);
      }
    }
  },

  onCustomerChange(e) {
    this.setData({ customerIndex: parseInt(e.detail.value) });
  },

  onInputRemark(e) {
    this.setData({ remark: e.detail.value });
  },

  addItem() {
    if (this.data.warehouseIndex === -1) {
      wx.showToast({ title: '请先选择发出仓库', icon: 'none' });
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

  async handleSubmitOutbound() {
    if (this.data.customerIndex === -1) {
      wx.showToast({ title: '请选择客户', icon: 'none' });
      return;
    }
    if (this.data.warehouseIndex === -1) {
      wx.showToast({ title: '请选择发出仓库', icon: 'none' });
      return;
    }

    const customer = this.data.customerList[this.data.customerIndex];
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
      const goods = this.data.availableGoods[it.goodsIndex];
      if (goods && goods.stockQuantity < qty) {
        wx.showModal({
          title: '库存不足',
          content: `货品 [${goods.name}] 当前库存仅 ${goods.stockQuantity} 件，无法出库 ${qty} 件！`,
          showCancel: false
        });
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
      partnerId: customer.id,
      warehouseId: warehouse.id,
      employeeId: app.globalData.userInfo ? app.globalData.userInfo.id : 1,
      remark: this.data.remark,
      items: postItems
    };

    try {
      await request.post('/outbound/create', payload);
      wx.showToast({ title: '出库成功', icon: 'success' });
      setTimeout(() => {
        wx.redirectTo({ url: '/pages/outbound/list' });
      }, 1000);
    } catch (e) {
      console.error(e);
    }
  }
});
