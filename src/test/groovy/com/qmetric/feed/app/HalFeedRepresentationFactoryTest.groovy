package com.qmetric.feed.app

import com.google.common.collect.ImmutableMap
import com.qmetric.feed.domain.*
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import spock.lang.Specification

import static com.google.common.collect.Lists.newArrayList
import static com.google.common.collect.Sets.newHashSet
import static com.qmetric.feed.domain.Links.NO_LINKS
import static com.theoryinpractise.halbuilder.api.RepresentationFactory.HAL_JSON
import static java.util.Collections.emptyMap
import static java.util.Collections.singletonMap

class HalFeedRepresentationFactoryTest extends Specification {

    final jsonSlurper = new JsonSlurper()

    final feedUri = new URI('http://localhost:1234/test-feed')

    final entry1 = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("someId", "aaaa", "value", "2000")))

    final entry2 = new FeedEntry(Id.of("2"), new DateTime(2013, 5, 14, 11, 2, 32), new Resource(ImmutableMap.of("someId", "bbbb", "value", "1000")))

    def "should return hal+json representation of entries"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithMultipleEntries.json').text)
    }

    def "should return hal+json representation of entries with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new FeedEntryLink("someLink", "http://other"), new FeedEntryLink("someLinkWithNamedParam", "http://other/{someId}"))))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithMultipleEntriesWithCustomLinks.json').text)
    }

    def "should return hal+json representation of entries with overridden self links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new FeedEntryLink("self", "http://other/{someId}"))))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithMultipleEntriesWithCustomSelfLinks.json').text)
    }

    def "should return hal+json representation of entry"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entry = entry1

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithSingleEntry.json').text)
    }

    def "should return hal+json representation of entry with complex properties"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, NO_LINKS)
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(ImmutableMap.of("nested", singletonMap("someId", "aaaa"), "arr", newArrayList("a", "b", "c"))))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithSingleEntryWithComplexProperties.json').text)
    }

    def "should return hal+json representation of entry with custom links"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new FeedEntryLink("someLink", "http://other"), new FeedEntryLink("someLinkWithNamedParam", "http://other/{someId}"))))
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(singletonMap("someId", "s1234")))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithSingleEntryWithCustomLinks.json').text)
    }

    def "should apply templated attr in returned hal+json representation with custom link with unresolved named parameter"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new FeedEntryLink("someLink", "http://other/{unresolved}"))))
        final entry = new FeedEntry(Id.of("1"), new DateTime(2013, 5, 13, 11, 2, 32), new Resource(emptyMap()))

        when:
        final hal = factory.format(entry)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithTemplatedCustomLink.json').text)
    }

    def "should apply templated attr in returned hal+json representation with custom self link with unresolved named parameter"()
    {
        given:
        final factory = new HalFeedRepresentationFactory(feedUri, new Links(newHashSet(new FeedEntryLink("self", "http://other/{unresolved}"))))
        final entries = new FeedEntries([entry2, entry1])

        when:
        final hal = factory.format(entries)

        then:
        jsonSlurper.parseText(hal.toString(HAL_JSON)) == jsonSlurper.parseText(this.getClass().getResource('/feed-samples/expectedHalWithMultipleEntriesWithTemplatedCustomSelfLinks.json').text)
    }
}
