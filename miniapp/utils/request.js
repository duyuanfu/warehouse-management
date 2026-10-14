// utils/request.js
const app = getApp();

function request(options) {
  const defaultApp = app || getApp();
  const baseUrl = defaultApp ? defaultApp.globalData.baseUrl : 'http://127.0.0.1:8080/api';
  const token = defaultApp ? defaultApp.globalData.token : (wx.getStorageSync('token') || '');

  return new Promise((resolve, reject) => {
    if (options.loading !== false) {
      wx.showLoading({
        title: options.loadingText || '加载中...',
        mask: true
      });
    }

    wx.request({
      url: `${baseUrl}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
        ...options.header
      },
      timeout: 15000,
      success(res) {
        if (options.loading !== false) {
          wx.hideLoading();
        }

        if (res.statusCode === 200) {
          const body = res.data;
          if (body.code === 200) {
            resolve(body.data);
          } else {
            wx.showModal({
              title: '提示',
              content: body.message || '操作失败',
              showCancel: false
            });
            reject(body);
          }
        } else if (res.statusCode === 409) {
          // 乐观锁冲突特别提醒
          wx.showModal({
            title: '并发冲突提示',
            content: (res.data && res.data.message) ? res.data.message : '该货品已被其他员工修改，请重新刷新！',
            showCancel: false
          });
          reject(res.data);
        } else {
          wx.showToast({
            title: `网络错误(${res.statusCode})`,
            icon: 'none'
          });
          reject(res);
        }
      },
      fail(err) {
        if (options.loading !== false) {
          wx.hideLoading();
        }
        wx.showModal({
          title: '连接失败',
          content: '无法连接到后端服务器，请检查后端是否已启动或 IP 是否配置正确',
          showCancel: false
        });
        reject(err);
      }
    });
  });
}

module.exports = {
  request,
  get: (url, data, options = {}) => request({ url, method: 'GET', data, ...options }),
  post: (url, data, options = {}) => request({ url, method: 'POST', data, ...options }),
  del: (url, data, options = {}) => request({ url, method: 'DELETE', data, ...options })
};
