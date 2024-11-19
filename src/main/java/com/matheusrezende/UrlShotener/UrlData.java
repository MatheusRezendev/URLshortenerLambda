package com.matheusrezende.UrlShotener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UrlData {
    private String originalUrl;
    private Long expirationTime;
}