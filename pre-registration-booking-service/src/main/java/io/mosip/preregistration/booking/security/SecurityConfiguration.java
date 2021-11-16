package io.mosip.preregistration.booking.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.*;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	private static final RequestMatcher PROTECTED_URLS = new AndRequestMatcher(
			new AntPathRequestMatcher("/appointment/**"),
			new NegatedRequestMatcher(new AntPathRequestMatcher("/appointment/booking-service/swagger-ui.html")),
			new NegatedRequestMatcher(new AntPathRequestMatcher("/**/v3/api-docs/**")),
			new NegatedRequestMatcher(new AntPathRequestMatcher("/**/v3/api-docs.yaml")),
			new NegatedRequestMatcher(new AntPathRequestMatcher("/**/swagger-ui/**")));

	AuthenticationProvider provider;

	public SecurityConfiguration(final AuthenticationProvider authenticationProvider) {
		super();
		this.provider = authenticationProvider;
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) {
		auth.authenticationProvider(provider);
	}

	@Override
	public void configure(final WebSecurity webSecurity) {
		webSecurity.ignoring().antMatchers("/**/assets/**", "/**/icons/**", "/**/screenshots/**", "/favicon**",
				"/**/favicon**", "/**/css/**", "/**/js/**", "/**/error**", "/**/webjars/**", "/**/v2/api-docs",
				"/**/configuration/ui", "/**/configuration/security", "/**/swagger-resources/**", "/**/swagger-ui.html",
				"/**/csrf", "/*/", "**/authenticate/**", "/**/actuator/**", "/**/authmanager/**", "/sendOtp",
				"/validateOtp", "/invalidateToken", "/config", "/login", "/logout", "/validateOTP", "/sendOTP",
				"/**/login", "/**/login/**", "/**/login-redirect/**", "/**/logout", "/**/h2-console/**",
				"/**/**/license/**", "/**/callback/**", "/**/authenticate/**");
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {

		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling().and()
				.authenticationProvider(provider)
				.addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class).authorizeRequests()
				.requestMatchers(PROTECTED_URLS).authenticated().and().csrf().disable().formLogin().disable()
				.httpBasic().disable().logout().disable();
	}

	@Primary
	@Bean
	AuthenticationFilter authenticationFilter() throws Exception {
		final AuthenticationFilter filter = new AuthenticationFilter(PROTECTED_URLS);
		filter.setAuthenticationManager(authenticationManager());
		return filter;
	}

	@Bean
	AuthenticationEntryPoint forbiddenEntryPoint() {
		return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
	}
}
