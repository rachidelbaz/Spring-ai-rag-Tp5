package com.example.springairag;

import groovy.lang.GString;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;

import java.util.List;

public class Test1 {
    public static void main(String[] args) {
        OpenAiApi openAiApi= new OpenAiApi(${OPEN_API_KEY});
        OpenAiChatModel openAiChatModel= new OpenAiChatModel(openAiApi, OpenAiChatOptions.builder().withModel("gpt-4o").withTemperature(0F).withMaxTokens(300).build());
        String systemMsgText= """
                Vous êtes un asistant spécialie dans le domaine de l'analyse des sentiment.
                Votre tâches est d'extraire à partir un commentaire le sentiment des différents aspects
                des ordinateurs achetés par des clients. Les aspects qui nous sont intéressent sont :
                L'écran, la souris et le clavier. le setiment peut être : positive, negative ou neutre
                Le résultat attendu sera au format JSON avec les champs suivants :
                - clavier : le sentiment relatif au clavier
                - souris : le sentiment relatif à la souris
                - ecran : le sentiment relatif à l'écran
                """;
        SystemMessage systemMessage = new SystemMessage(systemMsgText);
        String userInputText= """
                Je ne suis pas satifait par la qualite de l'écran,
                mais le clavier est mauvais alors que pour la souris la qualité est plotôt assez bonne.
                par ailleur le pense que cet ordinateur consome beacoup d'énergie
                """;
        UserMessage userMessage= new UserMessage(userInputText);
        String userInputText1 = """
            Je suis satifait par la qualité de l'écran,
            mais le clavier est mauvais alors que pour la souris la qualité est plotôt moyenne.
            par ailleur le pense que cet ordinateur sonsome beacoup d'énergie """;
        UserMessage userMessage1= new UserMessage(userInputText1);
        String response1 = """
        {
        "clavier": "negative",
          "souris": "neutre",
         "ecran": "positive"
        }""";
        AssistantMessage assistantMessage1= new AssistantMessage(response1);
        Prompt zeroShopPromt= new Prompt(List.of(systemMessage,userMessage1,assistantMessage1
                ,userMessage));
        ChatResponse chatResponse=openAiChatModel.call(zeroShopPromt);
        System.out.println(chatResponse.getResult().getOutput().getContent());

    }
}
