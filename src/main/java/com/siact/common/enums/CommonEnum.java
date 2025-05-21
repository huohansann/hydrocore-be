package com.siact.common.enums;

import com.siact.common.exception.BaseErrorInfoInterface;
import lombok.Getter;

@Getter
public enum CommonEnum implements BaseErrorInfoInterface {
  // 数据操作错误定义
  SUCCESS("200", "成功!"),
  BODY_NOT_MATCH("400", "请求的数据格式不符!"),
  SIGNATURE_NOT_MATCH("401", "请求的数字签名不匹配!"),
  NOT_FOUND("404", "未找到该资源!"),
  INTERNAL_SERVER_ERROR("500", "服务器内部错误!"),
  SERVER_BUSY("503", "服务器正忙，请稍后再试!"),
  MISS_VALUE("414", "缺失参数传递的值"),
  COLLECTION_EMPTY("400", "查询返回的集合内容为空"),
  OBJECT_NULL("400", "校验对象为null"),
  STRING_PARAM_BLANK("400", "String类型参数存在空或null，请核对参数"),
  TEMPLATE_MISMATCH("204", "导入文件与系统提供模板不相符，请重新导入！"),
  TEMPLATE_CONFIGURATION("204", "请联系管理员配置模板后再重新尝试！"),
  TEMPLATE_PARAM_NULL("204", "导入数据为空，请重新导入！"),
  TEMPLATE_TYPE_ERROR("204", "请检查导入文件格式！"),
  TEMPLATE_PARAM_ERROR("204", "导入数据格式有误，请检查后重新导入！！"),
  REQUEST_FAIL("1001", "请求数字孪生失败！"),
  REQUEST_STATUS_ABNORMAL("1002", "请求数字孪生返回状态异常！"),
  REQUEST_DATA_BLANK("1003", "请求数字孪生返回数据为空！"),
  REQUEST_PARAM_ABNORMAL("1004", "请求参数校验不通过！"),
  YEAR_REPEATED("601", "重复的年份！"),
  DATA_EMPTY("601", "数据为空！"),
  BUILDING_REPEATED("601", "办公楼重复！"),
  EXPORT_ERROR("601", "导出出错");

  private final String resultCode;
  private final String resultMsg;

  CommonEnum(String resultCode, String resultMsg) {
    this.resultCode = resultCode;
    this.resultMsg = resultMsg;
  }

}
