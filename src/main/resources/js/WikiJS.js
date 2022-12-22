
class PageTree {
    constructor(name, path, depth, pageId, title, contentType) { 
        this.name = name;
        this.path = path;
        this.depth = depth;
        this.pageId = pageId;
        this.title = title;
        this.contentType = contentType;
        this.childArray = [];
    }
    addParam(path, pageId, title, contentType) { 
        this.path = path;
        this.pageId = pageId;
        this.title = title;
        this.contentType = contentType;
    }
    addChild(path, pageId, title, contentType) { 
        var path_array = path.split("/");
        //var child_name = path_array[this.depth];
        //console.log('childArray include check: ' + path_array[this.depth] + ", depth:" + this.depth + ". Result " + (path_array[this.depth] in this.childArray) );
        if (path_array[this.depth] in this.childArray){
            if ((this.depth+1) in path_array){
                console.log('This childArray include ' + path_array[this.depth] + ". Add Child " + path + ", depth:"+this.depth );
                this.childArray[path_array[this.depth]].addChild(path, pageId, title, contentType);
            } else {
                console.log('This childArray include ' + path_array[this.depth] + ". Add Parametrs " + path + ", depth:"+this.depth );
                this.childArray[path_array[this.depth]].addParam(path, pageId, title, contentType);
            }   
        } else {
            if ((this.depth+1) in path_array){
                console.log('New prototype in childArray ' + path_array[this.depth] + " for "+ path + ", depth:"+this.depth + ". Add Child");
                this.childArray[path_array[this.depth]] = new PageTree(path_array[this.depth], "", this.depth+1, 0, "", "");
                this.childArray[path_array[this.depth]].addChild(path, pageId, title, contentType);
            } else {
                console.log('New full in childArray ' + path_array[this.depth] + " for "+ path + ", depth:"+this.depth);
                this.childArray[path_array[this.depth]] = new PageTree(path_array[this.depth], path, this.depth+1, pageId, title, contentType);
            }
        }
    }
    getName(){
        return this.name;
    }
    getHTML(){
        var documentlisthtml = "";
        if (this.depth > 0){
            documentlisthtml = documentlisthtml + "<li><a class=\"document\" href=\"" + window.location.origin + window.location.pathname + "?page=" + this.pageId + "\" id=\"page_" + this.pageId + "\" document-id=\"" + this.pageId + "\" target=\"page_content\" title=\"" + this.title + "\" path=\"" + this.path + "\">" + this.title + "</a></li>";
        }
        if (Object.keys(this.childArray).length > 0){
            if (this.depth > 0) {
                documentlisthtml = documentlisthtml + "<ul class=\"aui-nav\">";
            }
            Object.values(this.childArray).forEach(pages => {
                documentlisthtml = documentlisthtml + pages.getHTML();
            });
            if (this.depth > 0) {
                documentlisthtml = documentlisthtml + "</ul>";
            }
        }
        console.log(this.name + " depth " + this.depth + " child count " + Object.keys(this.childArray).length);
        return documentlisthtml;
    }
    
  }

var load_file_tree = function(response, target){
    let page_tree = new PageTree("root", "", 0, 0, "", "none");
    //var documentlisthtml = ;
    
    AJS.$('#' + target).html("");
    if (response.data.pages.list.length > 0) { 
        //console.log("Есть данные для: " + target);
        response.data.pages.list.forEach(function(pages) {
            page_tree.addChild(pages.path, pages.id, pages.title, pages.contentType);
            //documentlisthtml = documentlisthtml + "<li><a class=\"filter-link document\" href=\"" + window.location.origin + window.location.pathname + "?page=" + pages.id + "\" document-id=\"" + pages.id + "\" target=\"page_content\" title=\"" + pages.title + "\" path=\"" + pages.path + "\">" + pages.title + "</a></li>";
            //console.log("page_" + pages.id + " add");
        });
        //AJS.$('#' + target).html(documentlisthtml);
        AJS.$('#' + target).html(page_tree.getHTML());
    } else {
        //AJS.$('#' + target).html("<li class=\"no-suggestions\">Нет статей</li>");
        console.log("Нет статей " + target);
    }
    
}

var tree_toc = function(toc, level){
    var tochtml = "";
    console.log("toc: " + toc);
    if (toc.length > 0) { 
        toc.forEach(function(toc) {
            tochtml = tochtml + "<li><a class=\"filter-link\" href=\"" + toc.anchor + "\" title=\"" + toc.title + "\"> <span class=\"aui-icon aui-icon-small aui-iconfont-chevron-right\"></span>" + toc.title + "</a>";
            if (toc.children.length> 0){
                console.log("Дiти: " + toc.children);
                tochtml = tochtml + "<ul class=\"aui-nav\">" + tree_toc(toc.children, level+1) + "</ul>"
            }
            tochtml = tochtml + "</li>"
        });
    }
    return tochtml;
}

var parse_toc = function(string_toc){
    var tochtml = "";
    if($.trim(string_toc)!=''){
        var toc = jQuery.parseJSON(string_toc);
        tochtml = tree_toc(toc, 0);
    }
    return tochtml;
}

var load_document = function(response, target){
    var tochtml = "";
    if (response.data.pages.single.title != "") { 
        console.log("Есть заголовок: " + response.data.pages.single.title);
        AJS.$('#' + target + "_title").html(response.data.pages.single.title).removeClass("hidden");
        AJS.$('#' + target + "_description").html(response.data.pages.single.description).removeClass("hidden");
        AJS.$('#' + target).html(response.data.pages.single.render).removeClass("hidden");
        var tochtml = parse_toc(response.data.pages.single.toc);
        AJS.$('#toc-tree').html(tochtml).removeClass("hidden");
    } else {
        console.log("Нет данных " + target);
    }
}


var pageSearch = function(query, global_settings, target, targetToHide) { 
    /* AJS.$('#' + target).find("input[type=checkbox]:checked").each(function () {
        $(this).parent().parent().detach().appendTo("#assignee-checked");
    });
    if($.trim($("#assignee-checked").html())!=''){
        $("#assignee-checked").removeClass("hidden");
    }*/
    var documentlisthtml = "";
    if (query.length > 2) { 
        settings = global_settings;
        settings["data"] = JSON.stringify({
            query: "{\r\n  pages {\r\n    search (query: \"" + query + "\") {\r\n      results {\r\n        id\r\n        path\r\n        title\r\n        }\r\n      }\r\n  }\r\n}",
            variables: {}
        })
        AJS.$.ajax(settings).done(function (response) {
            console.log(response);
            AJS.$('#' + target).html("");
            if (response.data.pages.search.results.length > 0) { 
                console.log("Есть совпадения: " + query);
                response.data.pages.search.results.forEach(function(pages) {
                    documentlisthtml = documentlisthtml + "<li><a class=\"filter-link document\" href=\"" + window.location.origin + window.location.pathname + "?page=" + pages.id + "\" document-id=\"" + pages.id + "\" target=\"page_content\" title=\"" + pages.title + "\">" + pages.title + "</a></li>";
                    console.log("page_" + pages.id + " add");
                });
            } else {
                documentlisthtml = "<li class=\"no-suggestions\" style=\"font-style: italic;\">Ничего не найдено</li>";
                console.log("Нет статей " + target);
            }
            AJS.$('#' + target).html(documentlisthtml).parent().removeClass("hidden");
            AJS.$('#' + targetToHide).parent().addClass("hidden");
        }).fail(function(textStatus){
            console.log("ajax sendfail " + textStatus);
        });
    } else {
        AJS.$('#' + targetToHide).parent().removeClass("hidden");
        AJS.$('#' + target).html("").parent().addClass("hidden");
    }
};


AJS.toInit(function(){
    // if($.trim($("#assignee-checked").html())==''){
    //    $("#assignee-checked").addClass("hidden");
    //}

    var global_settings = { //"/graphql"
        "url": wiki_server + api_path,
        "method": "POST",
        "timeout": 0,
        "headers": {
          "Content-Type": "application/json",
          "Authorization": "Bearer " + key
        },
        "data": JSON.stringify({
          query: "",
          variables: {}
        })
    };

    settings = global_settings;
    settings["data"] = JSON.stringify({
        query: "{\r\n  pages {\r\n    list (orderBy: TITLE) {\r\n      id\r\n      path\r\n      title\r\n      contentType\r\n    }\r\n  }\r\n}",
        variables: {}
    })
    $.ajax(settings).done(function (response) {
        console.log(response);
        load_file_tree(response, "menu-tree");
    }).fail(function(textStatus){
        console.log("ajax sendfail " + textStatus);
    });

    settings = global_settings;
    settings["data"] = JSON.stringify({
        query: "{\r\n  pages {\r\n    single (id: 6) {\r\n      path\r\n      title\r\n      description\r\n      contentType\r\n      createdAt\r\n      updatedAt\r\n      render\r\n      toc\r\n    }\r\n  }\r\n}",
        variables: {}
      })
    $.ajax(settings).done(function (response) {
        console.log(response);
        load_document(response, "page_content");
    }).fail(function(textStatus){
        console.log("ajax sendfail " + textStatus);
    });

    AJS.$("#menu-tree, #page-suggestions").on("click", "a.document", function(){
        var doc_id = $(this).attr("document-id");
        $("#menu-tree .aui-nav-selected").each(function() { $(this).removeClass("aui-nav-selected");  });
        if ($(this).hasClass("filter-link")){
            AJS.$('#page-input').val('').trigger("keyup");
            $("#page_" + doc_id).parent().addClass("aui-nav-selected");
        }else{
            $(this).parent().addClass("aui-nav-selected");
        }

        settings = global_settings;
        settings["data"] = JSON.stringify({
            query: "{\r\n  pages {\r\n    single (id: " + doc_id + ") {\r\n      path\r\n      title\r\n      description\r\n      contentType\r\n      createdAt\r\n      updatedAt\r\n      render\r\n      toc\r\n    }\r\n  }\r\n}",
            variables: {}
          })
        $.ajax(settings).done(function (response) {
            console.log(response);
            load_document(response, "page_content");
        }).fail(function(textStatus){
            console.log("ajax sendfail " + textStatus);
        });
        return false;
    });

    
    AJS.$('#page-input').on("keyup", function(a) {
        var query = $(this).val(); 
        var target = $(this).attr("aria-controls");
        pageSearch(query, global_settings, target, "menu-tree");
        return false;
    }).on("keydown", function(a) {
        if(a.keyCode == 13){  a.preventDefault(); return false; }
    });
    
    AJS.$("button.tooltiped").tooltip();
    /*
    AJS.$("#edit_document").tooltip(); // заменить на общий селектор
    AJS.$("#new_document").tooltip();
    AJS.$("#new_comment_in_content_table").tooltip();
    AJS.$("#favorite").tooltip();
    AJS.$("#history_of_changes").tooltip();
    AJS.$("#share_it").tooltip();
    AJS.$("#print_version").tooltip();
    */
});

/*
AJS.$(document).ready(function() {

});
*/

