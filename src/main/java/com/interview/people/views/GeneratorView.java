package com.interview.people.views;

import com.interview.people.tools.RandomPeopleService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Route(value = "generator")
@PageTitle("People generator")
public class GeneratorView extends Div {

    @Autowired
    private RandomPeopleService randomPeopleService;

    @PostConstruct
    private void init(){

        Label label = new Label("Generate a .csv file with random people");

        final IntegerField numberField=new IntegerField("how many?");

        Button bStart = new Button("Start");
        bStart.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            try {
                int number = numberField.getValue();
                if(number>0){
                    randomPeopleService.generatePeople(number);
                    new Notification("File generated successfully in ~/people/ directory").open();
                }else{
                    new Notification("Input a valid number", 3000).open();
                }
            } catch (Exception e) {
                new Notification("Error generating random people file").open();
            }
        });

        VerticalLayout layout = new VerticalLayout();
        layout.add(label, numberField, bStart);
        add(layout);

    }


}
