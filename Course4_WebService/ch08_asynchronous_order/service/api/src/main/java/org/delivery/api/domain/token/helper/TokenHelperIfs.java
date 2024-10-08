package org.delivery.api.domain.token.helper;

import org.delivery.api.domain.token.model.TokenDto;

import java.util.Map;

public interface TokenHelperIfs {
    TokenDto issueAccessToken(Map<String, Object> data);

    TokenDto issueRefreshToken(Map<String, Object> data);

    Map<String, Object> validateAccessToken(String token);

}
