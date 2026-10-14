// pages/goods/edit.js
const request = require('../../utils/request');

Page({
  data: {
    isEdit: false,
    readonly: false,
    goodsId: null,
    goods: {
      skuCode: '',
      name: '',
      spec: '',
      warehouseId: null,
      stockQuantity: 0,
      safetyStock: 10,
      price: '',
      version: 0
    },
    warehouseList: [],
    warehouseIndex: -1
  },

  async onLoad(options) {
    await this.loadWarehouses();
    const readonly = options && options.readonly === 'true';
    this.setData({ readonly });

    if (options && options.id) {
      this.setData({ isEdit: true, goodsId: options.id });
      this.loadGoodsDetail(options.id);
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

  async loadGoodsDetail(id) {
    try {
      const data = await request.get(`/goods/${id}`);
      let whIndex = -1;
      if (this.data.warehouseList.length > 0) {
        whIndex = this.data.warehouseList.findIndex(w => w.id === data.warehouseId);
      }
      this.setData({
        goods: data,
        warehouseIndex: whIndex
      });
    } catch (e) {
      console.error(e);
    }
  },

  onInput(e) {
    if (this.data.readonly) return;
    const field = e.currentTarget.dataset.field;
    this.setData({
      [`goods.${field}`]: e.detail.value
    });
  },

  onWarehouseChange(e) {
    if (this.data.readonly) return;
    const idx = parseInt(e.detail.value);
    const selected = this.data.warehouseList[idx];
    this.setData({
      warehouseIndex: idx,
      'goods.warehouseId': selected.id
    });
  },

  async handleSubmit() {
    const g = this.data.goods;
    if (!g.skuCode || !g.skuCode.trim()) {
      wx.showToast({ title: '请输入SKU编码', icon: 'none' });
      return;
    }
    if (!g.name || !g.name.trim()) {
      wx.showToast({ title: '请输入货品名称', icon: 'none' });
      return;
    }
    if (!g.warehouseId) {
      wx.showToast({ title: '请选择存放仓库', icon: 'none' });
      return;
    }

    const payload = {
      id: this.data.goodsId,
      skuCode: g.skuCode.trim(),
      name: g.name.trim(),
      spec: g.spec ? g.spec.trim() : '',
      warehouseId: g.warehouseId,
      safetyStock: parseInt(g.safetyStock) || 10,
      price: parseFloat(g.price) || 0.00
    };
    if (!this.data.isEdit) {
      payload.stockQuantity = parseInt(g.stockQuantity) || 0;
    }

    try {
      await request.post('/goods/save', payload);
      wx.showToast({ title: '保存成功', icon: 'success' });
      setTimeout(() => {
        wx.navigateBack();
      }, 800);
    } catch (e) {
      console.error(e);
    }
  },

  async handleDelete() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除该货品吗？（若库存大于0将无法删除）',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request.del(`/goods/${this.data.goodsId}`);
            wx.showToast({ title: '删除成功', icon: 'success' });
            setTimeout(() => {
              wx.navigateBack();
            }, 800);
          } catch (e) {
            console.error(e);
          }
        }
      }
    });
  }
});
