package com.biztoi.web.client;

import com.biztoi.api.RakutenBooksTotalApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "rakutenBooksApi", url= "https://app.rakuten.co.jp/services/api", configuration = com.biztoi.web.config.FeignConfiguration.class)
public interface RakutenBooksApiClient extends RakutenBooksTotalApi {}
