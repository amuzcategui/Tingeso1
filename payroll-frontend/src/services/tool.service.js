import httpClient from '../http-common.js';

const getAllTools = () => httpClient.get('/tool/all');

const getAllToolsForAdmin = () => httpClient.get('/tool/inventory/all');


const saveTool = (toolData, rutAdmin) => {
  return httpClient.post('/tool/save', toolData, { params: { rutAdmin } });
};


const deactivateTool = (toolId, quantity, rutAdmin) => {
  return httpClient.put('/tool/deactivate', null, { 
    params: { 
      idTool: toolId, 
      quantityToDeactivate: quantity,
      rutCustomer: rutAdmin 
    } 
  });
};


const repairTool = (toolId, quantity, rutAdmin) => {
  return httpClient.put('/tool/repair', null, {
    params: {
      idTool: toolId,
      quantityToRepair: quantity,
      rutCustomer: rutAdmin 
    }
  });
};


const availableTool = (toolId, rutAdmin, quantity) => {
  return httpClient.put('/tool/available', null, {
    params: {
      idTool: toolId,
      rutCustomer: rutAdmin,
      quantityToActivate: quantity 
    }
  });
};


const updateToolFee = (toolId, newFee) => {
  return httpClient.put(`/tool/update-fee?id=${toolId}&fee=${newFee}`);
};


const updateReplacementValue = (toolId, newValue) => {
  return httpClient.put(`/tool/update-value?idTool=${toolId}&replacementValue=${newValue}`);
};

export {
  getAllTools,
  getAllToolsForAdmin,
  saveTool,
  deactivateTool,
  repairTool,
  availableTool,
  updateToolFee,
  updateReplacementValue
};