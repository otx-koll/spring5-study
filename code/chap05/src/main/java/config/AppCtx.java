package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import spring.MemberPrinter;
import spring.VersionPrinter;

@Configuration
@ComponentScan(basePackages = {"spring"})
public class AppCtx {
	
//	@Bean
//	@Qualifier("printer")
//	public MemberPrinter memberPrinter1() {
//		return new MemberPrinter();
//	}
//	
//	@Bean
//	@Qualifier("summaryPrinter")
//	public MemberSummaryPrinter memberPrinter2() {
//		return new MemberSummaryPrinter();
//	}
	
	@Bean
	public MemberPrinter memberPrinter() {
		return new MemberPrinter();
	}
	
	@Bean
	public VersionPrinter versionPrinter() {
		VersionPrinter versionPrinter = new VersionPrinter();
		versionPrinter.setMajorVersion(5);
		versionPrinter.setMinorVersion(0);
		return versionPrinter;
	}
}
