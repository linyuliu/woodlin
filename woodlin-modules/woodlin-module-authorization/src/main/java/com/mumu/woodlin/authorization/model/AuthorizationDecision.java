package com.mumu.woodlin.authorization.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 授权决策响应。
 *
 * @author mumu
 * @since 2026-06-02
 */
@Data
@Accessors(chain = true)
@Schema(description = "授权决策响应")
public class AuthorizationDecision implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "决策结果")
    private AuthorizationEffect effect = AuthorizationEffect.DENY;

    @Schema(description = "决策原因")
    private String reason;

    @Schema(description = "命中策略编码")
    private List<String> matchedPolicyCodes = new ArrayList<>();

    @Schema(description = "附加义务")
    private Map<String, Object> obligations = new LinkedHashMap<>();

    /**
     * 创建允许响应。
     *
     * @param reason 原因
     * @return 决策响应
     */
    public static AuthorizationDecision allow(String reason) {
        return new AuthorizationDecision().setEffect(AuthorizationEffect.ALLOW).setReason(reason);
    }

    /**
     * 创建拒绝响应。
     *
     * @param reason 原因
     * @return 决策响应
     */
    public static AuthorizationDecision deny(String reason) {
        return new AuthorizationDecision().setEffect(AuthorizationEffect.DENY).setReason(reason);
    }

    /**
     * 判断是否允许。
     *
     * @return 是否允许
     */
    public boolean isAllowed() {
        return AuthorizationEffect.ALLOW == effect;
    }
}
