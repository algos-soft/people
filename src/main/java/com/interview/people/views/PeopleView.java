package com.interview.people.views;

import com.interview.people.data.Person;
import com.interview.people.data.PersonService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
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

        Button bImport = new Button("Import");
        bImport.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            personService.test();
            grid.getDataProvider().refreshAll();
        });

        Button bDelete = new Button("Delete all");
        bDelete.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            personService.deleteAll();
            grid.getDataProvider().refreshAll();
        });


        VerticalLayout layout = new VerticalLayout();
        layout.setHeightFull();
        layout.setWidthFull();
        grid.setHeightFull();
        HorizontalLayout bLayout = new HorizontalLayout();
        bLayout.add(bImport, bDelete);
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

        grid.addColumn(Person::getFirstname).setHeader("first name");
        grid.addColumn(Person::getLastname).setHeader("last name");
        grid.addColumn(Person::getEmail).setHeader("email");

        return grid;

    }


}
