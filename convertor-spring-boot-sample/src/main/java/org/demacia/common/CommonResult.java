package org.demacia.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * @author hepenglin
 * @since 2025/12/26 15:58
 **/
@Data
@Schema(description = "通用返回结果")
public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "状态码", example = "200")
    private Integer code;

    @Schema(description = "返回消息", example = "操作成功")
    private String message;

    @Schema(description = "返回数据")
    private T data;

    @Schema(description = "时间戳", example = "1640995200000")
    private Long timestamp;

    public CommonResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success() {
        return success(null, "操作成功");
    }

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<>(200, message, data);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> error(String message) {
        return error(500, message);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> error(Integer code, String message) {
        return new CommonResult<>(code, message, null);
    }

    /**
     * 失败返回结果
     */
    public static <T> CommonResult<T> error(Integer code, String message, T data) {
        return new CommonResult<>(code, message, data);
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed() {
        return error(400, "参数验证失败");
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return error(400, message);
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized() {
        return error(401, "暂未登录或token已经过期");
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden() {
        return error(403, "没有相关权限");
    }
}