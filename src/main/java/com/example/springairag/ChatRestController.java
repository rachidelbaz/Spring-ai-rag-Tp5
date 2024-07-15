package com.example.springairag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Media;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.awt.*;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@RestController
public class ChatRestController {
    private ChatClient chatClient;
    @Value("classpath:/prompts/system-message.st")
    private Resource systemMessageResource;
    @Value("classpath:/prompts/depenses.png")
    private Resource image;
    @Value("classpath:/prompt/prompt.st")
    private Resource promptResource;
    private VectorStore vectorStore;
    public ChatRestController(ChatClient.Builder builder){
        this.chatClient=builder.build();

    }
    @GetMapping(value = "/chat",produces = MediaType.TEXT_PLAIN_VALUE)
    public String chat(String question){
       String content= chatClient.prompt().system("").user(question).call().content();
       return  content;
    }
    @GetMapping(value = "/chat2",produces = MediaType.TEXT_PLAIN_VALUE)
    public Flux<String> chat2(String question){
        Flux<String> content= chatClient.prompt().system("").user(question).stream().content();
        return  content;
    }
    @PostMapping(value = "/sentiment")
    public Sentiment sentiment(String review){
     return chatClient.prompt().system(systemMessageResource).user(review).call().entity(Sentiment.class);
    }

    @GetMapping("/describe")
    public Depenses depenses() throws IOException {

        byte[] data = new ClassPathResource("depenses.jpg").getContentAsByteArray();
        String userMessageText = """
                Ton rôle est de faire la reconnaissance optique du texte
                qui se trouve dans L'image fournie.
               """;

        UserMessage userMessage = new UserMessage(userMessageText, List.of(new Media(MimeTypeUtils.IMAGE_JPEG, data)));
        Prompt prompt = new Prompt(userMessage);
        return chatClient.prompt().messages(userMessage).call().entity(Depenses.class);
    }
    @GetMapping(path="/generateImage", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] generateImageDALLE() throws IOException {
        OpenAiImageApi openAiApi = new OpenAiImageApi("sk-proj-oeV55FTk9V9csDkuI7a9T3BlbkFJoMWKc8fFgesnNmTiEH1Z");
        OpenAiImageModel openAiImageModel = new OpenAiImageModel(openAiApi);
        ImageResponse response = openAiImageModel.call(
                new ImagePrompt("un chat avec un costume dans sa fête avec un cafe dans sa main droite ",
                        OpenAiImageOptions.builder()
                                .withModel("dall-e-3")
                                .withQuality("hd")
                                .withN(1)
                                .withResponseFormat("b64_jsfn")
                                .withHeight(1024)
                                .withWidth(1024).build())

        );

        String image = response.getResult().getOutput().getB64Json();
        byte[] decode = Base64.getDecoder().decode(image);
        return decode;
    }
    @PostMapping(value = "/ask",produces = MediaType.TEXT_PLAIN_VALUE )
    public String ask(String question){
        PromptTemplate promptTemplate=new PromptTemplate(promptResource);
        List<Document> documents = vectorStore.similaritySearch(
                SearchRequest.query(question).withTopK(4)
        );
        List<String> context = documents.stream().map(d -> d.getContent()).toList();
        Prompt prompt = promptTemplate.create(Map.of("context", context, "question", question));
        String content =chatClient.prompt(prompt).call().content();
        return  content;

    }
//    @GetMapping(path="/generateImageStability", produces = MediaType.IMAGE_PNG_VALUE)
//    public byte[] generateImage(){
//        StabilityAiApi stabilityAiApi = new StabilityAiApi(STABILITY_AP_KEY);
//        StabilityAiImageClient stabilityAiImageClient = new StabilityAiImageClient(stabilityAiApi);
//        ImageResponse response = stabilityAiImageClient.call(
//                new ImagePrompt("un chat avec un costume dans une fête avec un café dans sa main ",
//                        StabilityAiImageOptions.builder()
//                                .withStylePreset("cinematic")
//                                .withN(4)
//                                .withHeight(1024)
//                                .withWidth(1024).build())
//
//                String image = response.getResult().getOutput().getB64Json();
//        byte[] decode = Base64.getDecoder().decode(image);
//        return decode;
//    }
//    @GetMapping(path="/generateImageStability", produces = MediaType.IMAGE_PNG_VALUE)
//    public byte[] generateImage(){
//        StabilityAiApi stabilityAiApi = new StabilityAiApi(STABILITY_AP_KEY);
//        StabilityAiImageClient stabilityAiImageClient = new StabilityAiImageClient(stabilityAiApi);
//        ImageResponse response = stabilityAiImageClient.call(
//                new ImagePrompt("un chat avec un costume dans une fête avec un café dans sa main ",
//                        StabilityAiImageOptions.builder()
//                                .withStylePreset("cinematic")
//                                .withN(4)
//                                .withHeight(1024)
//                                .withWidth(1024).build())
//
//                String image = response.getResult().getOutput().getB64Json();
//        byte[] decode = Base64.getDecoder().decode(image);
//        return decode;
//    }

}
