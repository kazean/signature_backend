package org.delivery.api.domain.token.helper;

import java.util.Map;

public interface TokenHelperIfs {
    TokenDto issueAccessToken(Map<String, Object> data);

    ToeknDto issueRefreshToken(Map<String, Object> data);

    Map<String, Object> validationTokenWithThrow(String token);
}
