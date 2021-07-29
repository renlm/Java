package cn.renlm.plugins.MyCrawler;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.renlm.plugins.Common.IntToEnum;
import lombok.Getter;

/**
 * 页面链接类型
 * 
 * @author Renlm
 *
 */
public enum PageUrlType implements IntToEnum.IntValue {

	seed(0, "种子"), data(1, "数据"), unknown(-1, "未知");

	public static final String extraKey = "PageUrlTypeExtra";

	private final int type;

	@Getter
	private final String text;

	private PageUrlType(int type, String text) {
		this.type = type;
		this.text = text;
	}

	@Override
	public int value() {
		return this.type;
	}

	/**
	 * 标准化处理请求链接（去除无效参数，减少重复请求）
	 * 
	 * @param url               请求链接
	 * @param invalidParamNames 无效参数名（多个逗号分隔）
	 * @return
	 */
	public static final String standardUrl(String url, String invalidParamNames) {
		if (StrUtil.isBlank(url)) {
			return null;
		}

		String noQueryUrl = url.split("\\?")[0];
		String[] invalidParamNameArr = StrUtil.splitToArray(invalidParamNames, StrUtil.COMMA);
		UrlQuery urlQuery = UrlQuery.of(url, CharsetUtil.CHARSET_UTF_8);

		Map<CharSequence, CharSequence> param = new LinkedHashMap<>();
		BeanUtil.copyProperties(urlQuery.getQueryMap(), param);
		MapUtil.removeNullValue(param);
		MapUtil.removeAny(param, invalidParamNameArr);

		if (ObjectUtil.isEmpty(param)) {
			return noQueryUrl;
		}

		return noQueryUrl + "?" + UrlQuery.of(param).build(CharsetUtil.CHARSET_UTF_8);
	}
}