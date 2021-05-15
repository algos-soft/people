package com.interview.people.views;

import com.interview.people.data.Person;
import com.interview.people.data.PersonService;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.internal.MessageDigestUtil;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@Route(value = "people")
@PageTitle("People")
@RouteAlias("")
public class PeopleView extends Div {


    @Autowired
    private PersonService personService;

    @PostConstruct
    private void init(){

        Grid grid = createGrid();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setDropAllowed(false);
        //upload.setMaxFileSize(2097152); // max 2 MB

        upload.addSucceededListener(event -> {
            try {
                personService.importCsv(buffer.getInputStream());
            } catch (Exception e) {
                // keep the message generic - avoid giving technical info to the user
                // the detailed error is logged by the service
                new Notification("The file could not be processed", 3000).open();
            }
        });

        upload.addFileRejectedListener(event -> {
            int a = 87;
            int b=a;
            //new Notification(event.getErrorMessage(), 3000).open();
        });

        upload.getElement().addEventListener("file-remove", event -> {
            int a = 87;
            int b=a;
        });





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
        bLayout.add(upload, bDelete);
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


//
//    private Component createComponent(String mimeType, String fileName,
//                                      InputStream stream) {
//        if (mimeType.startsWith("text")) {
//            return createTextComponent(stream);
//        } else if (mimeType.startsWith("image")) {
//            Image image = new Image();
//            try {
//
//                byte[] bytes = IOUtils.toByteArray(stream);
//                image.getElement().setAttribute("src", new StreamResource(
//                        fileName, () -> new ByteArrayInputStream(bytes)));
//                try (ImageInputStream in = ImageIO.createImageInputStream(
//                        new ByteArrayInputStream(bytes))) {
//                    final Iterator<ImageReader> readers = ImageIO
//                            .getImageReaders(in);
//                    if (readers.hasNext()) {
//                        ImageReader reader = readers.next();
//                        try {
//                            reader.setInput(in);
//                            image.setWidth(reader.getWidth(0) + "px");
//                            image.setHeight(reader.getHeight(0) + "px");
//                        } finally {
//                            reader.dispose();
//                        }
//                    }
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            image.setSizeFull();
//            return image;
//        }
//        Div content = new Div();
//        String text = String.format("Mime type: '%s'\nSHA-256 hash: '%s'",
//                mimeType, MessageDigestUtil.sha256(stream.toString()));
//        content.setText(text);
//        return content;
//
//    }
//
//
//    private Component createTextComponent(InputStream stream) {
//        String text;
//        try {
//            text = IOUtils.toString(stream, StandardCharsets.UTF_8);
//        } catch (IOException e) {
//            text = "exception reading stream";
//        }
//        return new Text(text);
//    }
//
//    private void showOutput(String text, Component content,
//                            HasComponents outputContainer) {
//        HtmlComponent p = new HtmlComponent(Tag.P);
//        p.getElement().setText(text);
//        outputContainer.add(p);
//        outputContainer.add(content);
//    }




}
