package de.bbqb.backend.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


/**
 * @author Marius Degen
 */
@Configuration
@EnableWebSecurity
public class AppSecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //.cors(Customizer.withDefaults())
                .csrf().disable()
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/").permitAll()
                .mvcMatchers(HttpMethod.OPTIONS,"**/*").permitAll()
                .mvcMatchers(HttpMethod.GET, "/devices").permitAll()
                .mvcMatchers(HttpMethod.GET, "/devices/*").permitAll()
                .mvcMatchers(HttpMethod.GET, "/_ah/start").permitAll() // Required by GAE to start up an instance
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    // TODO: Configure CORS appropriately
    //@Bean
    //CorsConfigurationSource corsConfigurationSource() {
    //    CorsConfiguration configuration = new CorsConfiguration();
    //    configuration.setAllowedOrigins(Arrays.asList("https://example.com"));
    //    configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT"));
    //    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //    source.registerCorsConfiguration("/**", configuration);
    //    return source;
    //}
}
