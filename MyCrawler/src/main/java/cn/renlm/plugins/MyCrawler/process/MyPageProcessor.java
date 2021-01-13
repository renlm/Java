package cn.renlm.plugins.MyCrawler.process;

import cn.renlm.plugins.MyCrawler.data.MyProcessPage;

/**
 * 页面处理
 * 
 * @author renlm-a
 *
 * @param <T>
 */
@FunctionalInterface
public interface MyPageProcessor<T> {

	public void process(final MyProcessPage<T> myPage);

}