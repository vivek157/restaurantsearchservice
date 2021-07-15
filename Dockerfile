FROM openjdk:8

COPY ./target/restaurantsearchservice-*.jar restaurantsearchservice.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","restaurantsearchservice.jar"]


