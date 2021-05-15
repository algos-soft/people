package com.interview.people.views;

import com.interview.people.data.Person;
import com.interview.people.data.PersonService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Route(value = "people")
@PageTitle("People")
@RouteAlias("")
public class PeopleView extends Div {


    @Autowired
    private PersonService personService;


    @PostConstruct
    private void init(){

        Grid grid = createGrid();

        Label rowCount=new Label();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setDropAllowed(false);

        upload.addSucceededListener(event -> {
            try {

                personService.importCsv(buffer.getInputStream());
                grid.getDataProvider().refreshAll();
                rowCount.setText(personService.count()+" rows");

            } catch (Exception e) {
                // keep the message generic - avoid giving too much technical info to the user
                // the detailed error is logged by the service
                new Notification("The file could not be processed", 3000).open();
            }
        });

        Button bDelete = new Button("Delete all");
        bDelete.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            personService.deleteAll();
            grid.getDataProvider().refreshAll();
            rowCount.setText(personService.count()+" rows");
        });

        rowCount.setText(personService.count()+" rows");

        VerticalLayout layout = new VerticalLayout();
        layout.setHeightFull();
        layout.setWidthFull();
        grid.setHeightFull();
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        bLayout.add(upload, bDelete, rowCount);
        layout.add(grid, bLayout);
        setHeightFull();
        setWidthFull();
        add(layout);

    }


    private Grid createGrid(){

        CallbackDataProvider<Person, Void> provider;
        provider = DataProvider.fromCallbacks(fetchCallback -> {
            int offset = fetchCallback.getOffset();
            int limit = fetchCallback.getLimit();
            return personService.fetch(offset, limit).stream();
        }, countCallback -> {
            return personService.count();
        });

        Grid<Person> grid = new Grid();
        grid.setDataProvider(provider);

        grid.addColumn(Person::getId).setHeader("id").setWidth("5em");
        grid.addColumn(Person::getEmail).setHeader("email").setAutoWidth(true);
        grid.addColumn(Person::getLastname).setHeader("last name").setAutoWidth(true);
        grid.addColumn(Person::getFirstname).setHeader("first name").setAutoWidth(true);
        grid.addColumn(Person::getFiscalCode).setHeader("fiscal code").setAutoWidth(true);
        grid.addColumn(Person::getDescription).setHeader("description").setAutoWidth(true);
        grid.addColumn(Person::getLastAccessDate).setHeader("last access").setAutoWidth(true);

        return grid;

    }

//    private void refreshGrid(){
//        grid.getDataProvider().refreshAll();
//
//    }


}
