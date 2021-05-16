package com.interview.people.views;

import com.interview.people.data.Person;
import com.interview.people.data.PersonService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.Command;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

@Route(value = "people")
@PageTitle("People")
@RouteAlias("")
@Push
public class PeopleView extends Div {


    @Autowired
    private PersonService personService;

    private Grid<Person> grid;
    private Label rowCount;

    @PostConstruct
    private void init(){

        grid = createGrid();

        rowCount=new Label();

        Button bDelete = new Button("Delete all");
        bDelete.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            personService.deleteAll();
            refreshGrid();
        });

        refreshGrid();

        // listener to the import completed event in order to refresh the grid content when it happens
        UI ui = UI.getCurrent();
        personService.addImportListener(() -> ui.access((Command) () -> refreshGrid()));

        // layout the components on page
        VerticalLayout layout = new VerticalLayout();
        layout.setHeightFull();
        layout.setWidthFull();
        grid.setHeightFull();
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        bLayout.add(bDelete, rowCount);
        layout.add(grid, bLayout);
        setHeightFull();
        setWidthFull();
        add(layout);

    }


    private Grid<Person> createGrid(){

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

    private void refreshGrid(){
        grid.getDataProvider().refreshAll();
        rowCount.setText(personService.count()+" rows");
    }


}
