package org.jbpm.formbuilder.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jbpm.formbuilder.client.menu.items.CustomMenuItem;
import org.jbpm.formbuilder.server.form.FormEncodingServerFactory;
import org.jbpm.formbuilder.server.form.SaveMenuItemDTO;
import org.jbpm.formbuilder.server.menu.GuvnorMenuService;
import org.jbpm.formbuilder.server.xml.FormEffectDTO;
import org.jbpm.formbuilder.server.xml.ListMenuItemsDTO;
import org.jbpm.formbuilder.server.xml.ListOptionsDTO;
import org.jbpm.formbuilder.server.xml.ListValidationsDTO;
import org.jbpm.formbuilder.server.xml.PropertiesDTO;
import org.jbpm.formbuilder.shared.form.FormEncodingException;
import org.jbpm.formbuilder.shared.form.FormEncodingFactory;
import org.jbpm.formbuilder.shared.form.FormRepresentationDecoder;
import org.jbpm.formbuilder.shared.menu.FormEffectDescription;
import org.jbpm.formbuilder.shared.menu.MenuItemDescription;
import org.jbpm.formbuilder.shared.menu.MenuOptionDescription;
import org.jbpm.formbuilder.shared.menu.MenuService;
import org.jbpm.formbuilder.shared.menu.MenuServiceException;
import org.jbpm.formbuilder.shared.menu.ValidationDescription;
import org.jbpm.formbuilder.shared.rep.FormItemRepresentation;

@Path("/menu")
public class RESTMenuService {

    private final MenuService menuService = new GuvnorMenuService();
    
    public RESTMenuService() {
        FormEncodingFactory.register(FormEncodingServerFactory.getEncoder(), FormEncodingServerFactory.getDecoder());
    }
    
    @GET @Path("/items") 
    public Response listMenuItems() {
        ResponseBuilder builder = Response.noContent();
        try {
            Map<String, List<MenuItemDescription>> items = menuService.listMenuItems();
            ListMenuItemsDTO dto = new ListMenuItemsDTO(items);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (MenuServiceException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }

    @GET @Path("/options")
    public Response listMenuOptions() {
        ResponseBuilder builder = Response.noContent();
        try {
            List<MenuOptionDescription> options = menuService.listOptions();
            ListOptionsDTO dto = new ListOptionsDTO(options);
            builder = Response.ok(dto, MediaType.APPLICATION_XML);
        } catch (MenuServiceException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @GET @Path("/validations")
    public Response getValidations() {
        ResponseBuilder builder = Response.noContent();
        try {
            List<ValidationDescription> validations = menuService.listValidations();
            ListValidationsDTO dto = new ListValidationsDTO(validations);
            builder = Response.ok(dto);
        } catch (MenuServiceException e) {
            builder = Response.serverError();
        }
        return builder.build();
    }
    
    @POST @Path("/items")
    public Response saveMenuItem(SaveMenuItemDTO dto) {
        try {
            MenuItemDescription menuItem = toMenuItemDescription(dto);
            menuService.saveMenuItem(dto.getGroupName(), menuItem);
            return Response.status(Status.CREATED).build();
        } catch (MenuServiceException e) {
            return Response.status(Status.CONFLICT).build();
        }
    }

    private MenuItemDescription toMenuItemDescription(SaveMenuItemDTO dto) throws MenuServiceException {
        FormRepresentationDecoder decoder = FormEncodingFactory.getDecoder();
        String json = dto.getClone();
        MenuItemDescription menuItem = new MenuItemDescription();
        try {
            FormItemRepresentation item = decoder.decodeItem(json);
            menuItem.setClassName(CustomMenuItem.class.getName());
            menuItem.setItemRepresentation(item);
            menuItem.setName(dto.getName());
            List<FormEffectDescription> effects = new ArrayList<FormEffectDescription>();
            if (dto.getEffect() != null) {
                for (FormEffectDTO effectDto : dto.getEffect()) {
                    FormEffectDescription effect = new FormEffectDescription();
                    effect.setClassName(effectDto.getClassName());
                    effects.add(effect);
                }
            }
            menuItem.setEffects(effects);
        } catch (FormEncodingException e) {
            throw new MenuServiceException("Couldn't load formRepresentation from dto", e); 
        }
        return menuItem;
    }
    
    @DELETE @Path("/items")
    public Response deleteMenuItem(SaveMenuItemDTO dto) {
        try {
            MenuItemDescription menuItem = toMenuItemDescription(dto);
            Map<String, List<MenuItemDescription>> items = menuService.listMenuItems();
            List<MenuItemDescription> group = items.get(dto.getGroupName());
            if (group == null || group.isEmpty()) {
                return Response.noContent().build();
            }
            if (!group.contains(menuItem)) {
                return Response.status(Status.CONFLICT).build();
            }
            menuService.deleteMenuItem(dto.getGroupName(), menuItem);
            return Response.status(Status.ACCEPTED).build();
        } catch (MenuServiceException e) {
            return Response.status(Status.CONFLICT).build();
        }
    }

    @GET @Path("/mappings")
    public Response getRepresentationMappings() {
        try {
            Map<String, String> props = menuService.getFormBuilderProperties();
            PropertiesDTO dto = new PropertiesDTO(props);
            return Response.ok(dto).build();
        } catch (MenuServiceException e) {
            return Response.serverError().build();
        }
    }
}
