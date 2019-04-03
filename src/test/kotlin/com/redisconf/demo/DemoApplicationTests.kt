package com.redisconf.demo

import org.hamcrest.Matcher
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.reactivestreams.Publisher
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.context.junit4.SpringRunner
import reactor.core.publisher.DirectProcessor
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import redis.embedded.RedisServer
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong

@DataRedisTest
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RedisDemoTests {

	val log: org.slf4j.Logger = org.slf4j.LoggerFactory.getLogger(RedisDemoTests::class.java)

	private val port = 6777

	private lateinit var redisServer: RedisServer

	private lateinit var lettuce: LettuceConnectionFactory

	private lateinit var template: ReactiveRedisTemplate<String, String>

	@BeforeAll
	fun setupRedis() {
		redisServer = RedisServer(port)

		redisServer.start()

		lettuce = LettuceConnectionFactory(RedisStandaloneConfiguration("127.0.0.1", port))

		lettuce.afterPropertiesSet()

		template = RedisDemoConfig().stringCache(lettuce)
	}

	@AfterAll
	fun tearDown() = redisServer.stop()

	private val valueMatcher: Matcher<String> = Matchers
			.allOf(
					Matchers.notNullValue(),
					Matchers.equalToIgnoringCase("1234")
			)

	@Test
	fun testShouldPing() {

		val ping = template.connectionFactory.reactiveConnection.ping()

		StepVerifier
				.create(ping)
				.expectSubscription()
				.expectNext("PONG")
				.verifyComplete()
	}

}