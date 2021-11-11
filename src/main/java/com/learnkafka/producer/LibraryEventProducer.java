package com.learnkafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learnkafka.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    KafkaTemplate<Integer,String> kafkaTemplate;
    @Autowired
    ObjectMapper mapper;

    String topic = "library-events";

    public void sendLibraryEvent(final LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.sendDefault(key,value);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key,value,ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key,value,result);
            }
        });

    }

    public void sendLibraryEvent_Approach3(final LibraryEvent libraryEvent) throws JsonProcessingException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);

        ProducerRecord<Integer,String> producerRecord = buildProducerRecord(key,value,topic);
        ListenableFuture<SendResult<Integer,String>> listenableFuture = kafkaTemplate.send(producerRecord);
        listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key,value,ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key,value,result);
            }
        });

    }

    private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value, String topic) {
        List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
        return new ProducerRecord<>(topic,null,key,value,null);
    }

    public SendResult<Integer, String> sendLibraryEventSynchronous(final LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {
        Integer key = libraryEvent.getLibraryEventId();
        String value = mapper.writeValueAsString(libraryEvent);
        SendResult<Integer,String> sendResult = null;

        try {
            sendResult = kafkaTemplate.sendDefault(key,value).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            log.error("InterruptedException/ExecutionException sending the message and the exception is {}",e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Exception sending the message and the exception is {}",e.getMessage());
            throw e;
        }
        return sendResult;
    }

    private void handleFailure(final Integer key, final String value, final Throwable ex) {
        log.error("Error sending the message and the exception is {}",ex.getMessage());
        try {
            throw ex;
        } catch (Throwable e) {
            log.error("Error in onFailure: {}",e.getMessage());
        }
    }

    private void handleSuccess(final Integer key, final String value, final SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key : {} and the value is {}, partition is {}", key,value,result.getRecordMetadata().partition());
    }
}
