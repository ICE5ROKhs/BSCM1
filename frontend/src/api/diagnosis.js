import { createApiInstance } from "../utils/api";

const api = createApiInstance();

export const diagnosisApi = {
  // 提交诊断
  submitDiagnosis: async (data) => {
    const formData = new FormData();
    formData.append("symptoms", data.symptoms);
    if (data.images) {
      data.images.forEach((image, index) => {
        formData.append(`images`, image);
      });
    }
    return await api.post("/diagnosis/submit", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
  },

  // 获取诊断历史
  getHistory: async () => {
    return await api.get("/diagnosis/history");
  },

  // 删除记录
  deleteRecord: async (id) => {
    return await api.delete(`/diagnosis/${id}`);
  },

  // 获取诊断详情
  getDetail: async (id) => {
    return await api.get(`/diagnosis/${id}`);
  },
};
