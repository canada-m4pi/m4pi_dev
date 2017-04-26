$(document).ready(function() {

  // Variables
  var $codeSnippets = $('.code-example-body'),
      $nav = $('.navbar'),
      $navitem=$('.navbar-item')
      $body = $('body'),
      $window = $(window),
      $popoverLink = $('[data-popover]'),
      navOffsetTop = $nav.offset().top,
      $document = $(document),
      entityMap = {
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': '&quot;',
        "'": '&#39;',
        "/": '&#x2F;'
      }

  function init() {
    $window.on('scroll', onScroll)
    $window.on('resize', resize)
    $popoverLink.on('mouseover', openPopover)
    $nav.mouseleave(closePopover)
    $navitem.mouseleave(closePopover)
    $('a[href^="#"]').on('click', smoothScroll)
    buildSnippets();
    $body.addClass('has-docked-nav')
  }

  function smoothScroll(e) {
    e.preventDefault();
    $(document).off("scroll");
    var target = this.hash,
        menu = target;
    $target = $(target);
    if($('.content-nav').length){

    $('html, body').stop().animate({
        'scrollTop': $target.offset().top-40
    }, 0, 'swing', function () {
        window.location.hash = target;
        $(document).on("scroll", onScroll);
    });

    }
  }

  function openPopover(e) {
    
    e.preventDefault()
    var popover = $($(this).data('popover'));
    	if(!$('.popover').hasClass('open')){
    		popover.toggleClass('open')
    		e.stopImmediatePropagation();
	}
  }

  function closePopover(e) {

    if($('.popover.open').length > 0) {
      $('.popover').removeClass('open')
    }
  }

  $("#button").click(function() {
    $('html, body').animate({
        scrollTop: $("#elementtoScrollToID").offset().top
    }, 2000);
});

  function resize() {
    $body.removeClass('has-docked-nav')
    navOffsetTop = $nav.offset().top
    onScroll();
  }

  function onScroll() {
    if(navOffsetTop < $window.scrollTop() && !$body.hasClass('has-docked-nav')) {
      $body.addClass('has-docked-nav')
    }
    if(navOffsetTop > $window.scrollTop() && $body.hasClass('has-docked-nav')) {
      $body.removeClass('has-docked-nav')
    }
  }

  function escapeHtml(string) {
    return String(string).replace(/[&<>"'\/]/g, function (s) {
      return entityMap[s];
    });
  }

  function buildSnippets() {
    $codeSnippets.each(function() {
      var newContent = escapeHtml($(this).html())
      $(this).html(newContent)
    })
  }


  init();


    /* Every time the window is scrolled ... */
    $(window).scroll( function(){

        /* Check the location of each desired element */
        $('.row').each( function(i){

            var bottom_of_object = $(this).offset().top + $(this).outerHeight();
            var bottom_of_window = $(window).scrollTop() + $(window).height();

            /* If the object is completely visible in the window, fade it it */
            if( bottom_of_window > bottom_of_object*0.70 ){

                $(this).animate({'opacity':'1'},1500);

            }

        });

    });

});
