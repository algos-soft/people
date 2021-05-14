package com.interview.people.views;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouteAlias;

@Route(value = "people")
@PageTitle("People")
public class PeopleView extends Div {

    public PeopleView() {
        addClassName("people-view");
        add(new Text("Content placeholder"));
    }

}
