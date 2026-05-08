package cn.dong.coade.modules.cmt.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@TableName("vehicleinfo")
public class PmsVehicleInfo {
    @Schema(description = "主键ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Schema(description = "车牌号")
    @TableField("plateno")
    private String plateNo;

    @Schema(description = "车牌颜色")
    @TableField("platecolor")
    private Integer plateColor;

    @Schema(description = "车牌类型")
    @TableField("platetype")
    private Integer plateType;

    @Schema(description = "车辆颜色")
    @TableField("vehiclecolor")
    private Integer vehicleColor;

    @Schema(description = "车辆类型")
    @TableField("vehicletype")
    private Integer vehicleType;

    @Schema(description = "停车类型")
    @TableField("parkingtype")
    private Integer parkingType;

    @Schema(description = "卡号")
    @TableField("cardno")
    private String cardNo;

    @Schema(description = "品牌")
    @TableField("brand")
    private String brand;

    @Schema(description = "车主姓名")
    @TableField("ownername")
    private String ownerName;

    @Schema(description = "车主地址")
    @TableField("owneraddress")
    private String ownerAddress;

    @Schema(description = "车主电话")
    @TableField("ownerphonenumber")
    private String ownerPhoneNumber;

    @Schema(description = "身份证号")
    @TableField("identitynumber")
    private String identityNumber;

    @Schema(description = "注册时间")
    @TableField("registertime")
    private LocalDateTime registerTime;

    @Schema(description = "开始时间")
    @TableField("begintime")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    @TableField("endtime")
    private LocalDateTime endTime;

    @Schema(description = "车主性别")
    @TableField("ownergender")
    private Integer ownerGender;

    @Schema(description = "车主工作单位")
    @TableField("ownerworkplace")
    private String ownerWorkplace;

    @Schema(description = "车主部门")
    @TableField("ownerdepartment")
    private String ownerDepartment;

    @Schema(description = "车主岗位")
    @TableField("ownerpost")
    private String ownerPost;

    @Schema(description = "唯一编号")
    @TableField("uniquenumber")
    private String uniqueNumber;

    @Schema(description = "所属类别")
    @TableField("categorybelonged")
    private Integer categoryBelonged;

    @Schema(description = "过期处理方式")
    @TableField("expiredhandling")
    private Integer expiredHandling;

    @Schema(description = "匹配处理方式")
    @TableField("matchedhandling")
    private Integer matchedHandling;

    @Schema(description = "时间戳")
    @TableField("\"timestamp\"")
    private LocalDateTime timestamp;

    @Schema(description = "预留字段1")
    @TableField("reserve1")
    private Integer reserve1;

    @Schema(description = "预留字段2")
    @TableField("reserve2")
    private String reserve2;

    @Schema(description = "停车权限")
    @TableField("parkpermission")
    private String parkPermission;

    @Schema(description = "车辆信息卡类型")
    @TableField("vehicleinfo_cardtype")
    private Integer vehicleInfoCardType;

    @Schema(description = "所属分组")
    @TableField("groupbelonged")
    private Integer groupBelonged;

    @Schema(description = "是否已包")
    @TableField("isalreaybag")
    private Integer isAlreayBag;

    @Schema(description = "是否来自第三方系统")
    @TableField("isfromthirdsystem")
    private Integer isFromThirdSystem;

    @Schema(description = "上传标识")
    @TableField("uploadflag")
    private Integer uploadFlag;

    @Schema(description = "历史时间")
    @TableField("historytime")
    private String historyTime;

    @Schema(description = "扩展信息")
    @TableField("extrainfo")
    private String extraInfo;

    @Schema(description = "每日时间")
    @TableField("dailytime")
    private String dailyTime;

    @Schema(description = "备注")
    @TableField("remark")
    private String remark;

    @Schema(description = "版本号")
    @TableField("version")
    private Integer version;

    @Schema(description = "周期类型信息")
    @TableField("periodtypeinfo")
    private String periodTypeInfo;
}
