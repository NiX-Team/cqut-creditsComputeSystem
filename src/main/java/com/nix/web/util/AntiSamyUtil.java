package com.nix.web.util;

import org.owasp.validator.html.AntiSamy;
import org.owasp.validator.html.CleanResults;
import org.owasp.validator.html.Policy;

public class AntiSamyUtil {
	public static String getCleanHTML(String data) {
		AntiSamy antiSamy = new AntiSamy();
		try {
			Policy policy = Policy.getInstance(AntiSamy.class.getResource("/antisamy-myspace.xml").openStream());
			CleanResults cleanResults = antiSamy.scan(data, policy);
			return cleanResults.getCleanHTML();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		String data = " <form>\n" +
				"        <table class=\"input tabContent\">\n" +
				"            <tr>\n" +
				"                <th>\n" +
				"                    开始日期:\n" +
				"                </th>\n" +
				"                <td>\n" +
				"                    <input type=\"text\"  name=\"start\" class=\"text Wdate\" value=\"\" title=\"开始日期\" placeholder=\"不输入默认起始日期不限\" onfocus=\"WdatePicker({maxDate: '#F{$dp.$D(\\'endDate\\')}'});\" >\n" +
				"                </td>\n" +
				"            </tr>\n" +
				"            <tr>\n" +
				"                <th>\n" +
				"                    结束日期:\n" +
				"                </th>\n" +
				"                <td>\n" +
				"                    <input type=\"text\" name=\"end\" class=\"text Wdate\" value=\"\" title=\"结束日期\" placeholder=\"不输入默认结束日期不限\" onfocus=\"WdatePicker({minDate: '#F{$dp.$D(\\'beginDate\\')}'});\" >\n" +
				"                </td>\n" +
				"            </tr>\n" +
				"            <tr>\n" +
				"                <th>\n" +
				"                    导出订单数量:\n" +
				"                </th>\n" +
				"                <td>\n" +
				"                    <input type=\"text\"  class=\"text\"  name=\"number\" value=\"\" placeholder=\"不输入默认导出日期内全部\"/>\n" +
				"                </td>\n" +
				"            </tr>\n" +
				"            <tr>\n" +
				"                <td>\n" +
				"                    &nbsp;\n" +
				"                </td>\n" +
				"                <td>\n" +
				"                    <input type=\"button\" class=\"button\" onclick=\"\" value=\"确认导出\">\n" +
				"                    <input type=\"button\" class=\"button\" onclick=\"\" value=\"重置\">\n" +
				"                </td>\n" +
				"            </tr>\n" +
				"        </table>\n" +
				"    </form>";
		System.out.println("Before:\n" + data);
		System.out.println();
		System.out.println("After:\n" + getCleanHTML(data));
	}
}
