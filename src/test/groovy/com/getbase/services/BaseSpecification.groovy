// WARNING: This code is auto-generated from the BaseCRM API Discovery JSON Schema

package com.getbase.services;

import com.getbase.Configuration
import com.getbase.Client
import spock.lang.Shared
import spock.lang.Specification

import com.getbase.models.*

abstract class BaseSpecification  extends Specification {
    def getAccessToken() {
        def token = System.getenv("BASECRM_ACCESS_TOKEN")
        if (token == null || token.isEmpty())
            throw new RuntimeException('"BASECRM_ACCESS_TOKEN" environment variable was not found.')
        token
    }

    def getConfiguration() {
        new Configuration.Builder()
                .verbose()
                .accessToken(accessToken)
                .userAgent(Configuration.DEFAULT_USER_AGENT + "+tests")
                .build()
    }

    def getClient() {
        new Client(configuration)
    }

    def nextRand() {
        Long.toString(new Random().nextLong())
    }


    def createAssociatedContact(attributes = [:]) {
        def dealId = createDeal().id;
        def associatedContactAttributes = [
            'role': "involved",
            'contact_id': createContact().id,
        ]
        associatedContactAttributes << attributes
        def associatedContact = client.associatedContacts().create(dealId, associatedContactAttributes);

        associatedContact.metaClass.dealId = dealId
        return associatedContact;
    }

    def createContact(attributes = [:]) {
        def contactAttributes = [
            'description': "I know him via Tom",
            'email': "mark@designservices.com",
            'facebook': "mjohnson",
            'fax': "+44-208-1234567",
            'first_name': 'Mark' +  nextRand(),
            'industry': "Design Services",
            'is_organization': false,
            'last_name': 'Johnson' +  nextRand(),
            'linkedin': "mjohnson",
            'mobile': "508-778-6516",
            'name': 'Design Services Company' +  nextRand(),
            'phone': "508-778-6516",
            'skype': "mjohnson",
            'title': "CEO",
            'twitter': "mjohnson",
            'website': "www.designservices.com",
            'tags': ["important"],
        ]
        contactAttributes << attributes
        def contact = client.contacts().create(contactAttributes);

        return contact;
    }

    def createDeal(attributes = [:]) {
        def dealAttributes = [
            'currency': "EUR",
            'hot': true,
            'name': 'Website Redesign' +  nextRand(),
            'value': 1000,
            'tags': ["important"],
            'contact_id': createContact().id,
        ]
        dealAttributes << attributes
        def deal = client.deals().create(dealAttributes);

        return deal;
    }

    def createLead(attributes = [:]) {
        def leadAttributes = [
            'description': "I know him via Tom",
            'email': "mark@designservices.com",
            'facebook': "mjohnson",
            'fax': "+44-208-1234567",
            'first_name': 'Mark' +  nextRand(),
            'industry': "Design Services",
            'last_name': 'Johnson' +  nextRand(),
            'linkedin': "mjohnson",
            'mobile': "508-778-6516",
            'phone': "508-778-6516",
            'skype': "mjohnson",
            'status': "Unqualified",
            'title': "CEO",
            'twitter': "mjohnson",
            'website': "www.designservices.com",
            'tags': ["important"],
        ]
        leadAttributes << attributes
        def lead = client.leads().create(leadAttributes);

        return lead;
    }

    def createLossReason(attributes = [:]) {
        def lossReasonAttributes = [
            'name': 'We were too expensive' +  nextRand(),
        ]
        lossReasonAttributes << attributes
        def lossReason = client.lossReasons().create(lossReasonAttributes);

        return lossReason;
    }

    def createNote(attributes = [:]) {
        def noteAttributes = [
            'content': "Highly important.",
            'resource_id': createContact().id,
            'resource_type': 'contact',
        ]
        noteAttributes << attributes
        def note = client.notes().create(noteAttributes);

        return note;
    }

    def createSource(attributes = [:]) {
        def sourceAttributes = [
            'name': 'Word of mouth' +  nextRand(),
        ]
        sourceAttributes << attributes
        def source = client.sources().create(sourceAttributes);

        return source;
    }

    def createTag(attributes = [:]) {
        def tagAttributes = [
            'name': 'publisher' +  nextRand(),
            'resource_type': 'contact',
        ]
        tagAttributes << attributes
        def tag = client.tags().create(tagAttributes);

        return tag;
    }

    def createTask(attributes = [:]) {
        def taskAttributes = [
            'content': "Contact Tom",
            'due_date': "2014-09-27T16:32:56+00:00",
            'remind_at': "2014-09-29T15:32:56+00:00",
            'resource_id': createContact().id,
            'resource_type': 'contact',
        ]
        taskAttributes << attributes
        def task = client.tasks().create(taskAttributes);

        return task;
    }
}
