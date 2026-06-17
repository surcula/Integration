"use strict";

(function () {
  var sidebarStorageKey = "adminHMD.sidebarMini";
  var sidebarScrollStorageKey = "adminHMD.sidebarScrollTop";
  var themeStorageKey = "adminHMD.colorTheme";
  var desktopMedia = "(min-width: 992px)";

  function onReady(callback) {
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", callback);
      return;
    }

    callback();
  }

  function isDesktop() {
    return window.matchMedia(desktopMedia).matches;
  }

  function canUseStorage() {
    try {
      var testKey = sidebarStorageKey + ".test";
      window.localStorage.setItem(testKey, "1");
      window.localStorage.removeItem(testKey);
      return true;
    } catch (error) {
      return false;
    }
  }

  function getSavedMiniState(storageAvailable) {
    if (!storageAvailable) {
      return false;
    }

    return window.localStorage.getItem(sidebarStorageKey) === "true";
  }

  function saveMiniState(storageAvailable, isMini) {
    if (storageAvailable) {
      window.localStorage.setItem(sidebarStorageKey, String(isMini));
    }
  }

  function getSavedSidebarScroll() {
    try {
      return window.sessionStorage.getItem(sidebarScrollStorageKey);
    } catch (error) {
      return null;
    }
  }

  function saveSidebarScroll(scrollTop) {
    try {
      window.sessionStorage.setItem(sidebarScrollStorageKey, String(scrollTop));
    } catch (error) {
      // Ignore storage errors: the sidebar can still work without scroll memory.
    }
  }

  function getPreferredTheme(storageAvailable) {
    var savedTheme = storageAvailable ? window.localStorage.getItem(themeStorageKey) : "";

    if (savedTheme === "dark" || savedTheme === "light") {
      return savedTheme;
    }

    if (window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches) {
      return "dark";
    }

    return "light";
  }

  onReady(function () {
    var body = document.body;
    var sidebar = document.querySelector(".admin-sidebar");
    var sidebarToggle = document.querySelector("[data-sidebar-toggle]");
    var themeToggles = document.querySelectorAll("[data-theme-toggle]");
    var themeIcons = document.querySelectorAll("[data-theme-icon]");
    var closeButtons = document.querySelectorAll("[data-sidebar-close]");
    var sidebarLinks = document.querySelectorAll(".sidebar-nav .nav-link");
    var mediaQuery = window.matchMedia(desktopMedia);
    var storageAvailable = canUseStorage();

    function initValidation() {
      var forms = document.querySelectorAll(".needs-validation");

      Array.prototype.forEach.call(forms, function (form) {
        form.addEventListener("submit", function (event) {
          if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
          }

          form.classList.add("was-validated");
        });
      });
    }

    function initTableSearch() {
      var searchInputs = document.querySelectorAll("[data-table-search]");

      Array.prototype.forEach.call(searchInputs, function (input) {
        var tableId = input.getAttribute("data-table-search");
        var table = document.getElementById(tableId);

        if (!table) {
          return;
        }

        input.addEventListener("input", function () {
          var query = input.value.trim().toLowerCase();
          var rows = table.querySelectorAll("tbody tr");

          Array.prototype.forEach.call(rows, function (row) {
            row.hidden = query !== "" && row.textContent.toLowerCase().indexOf(query) === -1;
          });
        });
      });
    }

    function updateThemeControls(theme) {
      var nextTheme = theme === "dark" ? "light" : "dark";
      var label = "Switch to " + nextTheme + " mode";
      var iconClass = theme === "dark" ? "bi bi-sun" : "bi bi-moon-stars";

      Array.prototype.forEach.call(themeToggles, function (button) {
        button.setAttribute("aria-label", label);
        button.setAttribute("title", label);
      });

      Array.prototype.forEach.call(themeIcons, function (icon) {
        icon.className = iconClass;
      });
    }

    function applyTheme(theme) {
      document.documentElement.setAttribute("data-theme", theme);
      document.documentElement.setAttribute("data-bs-theme", theme);

      if (storageAvailable) {
        window.localStorage.setItem(themeStorageKey, theme);
      }

      updateThemeControls(theme);
    }

    function initThemeToggle() {
      applyTheme(getPreferredTheme(storageAvailable));

      Array.prototype.forEach.call(themeToggles, function (button) {
        button.addEventListener("click", function () {
          var currentTheme = document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
          applyTheme(currentTheme === "dark" ? "light" : "dark");
        });
      });
    }

    function initSidebarScrollMemory() {
      if (!sidebar) {
        return;
      }

      var savedScroll = getSavedSidebarScroll();

      if (savedScroll !== null) {
        var scrollTop = parseInt(savedScroll, 10);

        if (!isNaN(scrollTop)) {
          var restoreScroll = function () {
            sidebar.scrollTop = scrollTop;
          };

          if (window.requestAnimationFrame) {
            window.requestAnimationFrame(restoreScroll);
          } else {
            restoreScroll();
          }
        }
      }

      sidebar.addEventListener("scroll", function () {
        saveSidebarScroll(sidebar.scrollTop);
      });

      Array.prototype.forEach.call(sidebarLinks, function (link) {
        link.addEventListener("click", function () {
          saveSidebarScroll(sidebar.scrollTop);
        });
      });
    }

    initValidation();
    initTableSearch();
    initThemeToggle();
    initSidebarScrollMemory();



    if (!sidebarToggle) {
      return;
    }

    function setClass(element, className, enabled) {
      if (enabled) {
        element.classList.add(className);
      } else {
        element.classList.remove(className);
      }
    }

    function setToggleExpanded() {
      var expanded = isDesktop()
        ? !body.classList.contains("sidebar-mini")
        : body.classList.contains("sidebar-open");

      sidebarToggle.setAttribute("aria-expanded", String(expanded));
    }

    function closeMobileSidebar() {
      body.classList.remove("sidebar-open");
      setToggleExpanded();
    }

    function toggleSidebar() {
      if (isDesktop()) {
        body.classList.toggle("sidebar-mini");
        saveMiniState(storageAvailable, body.classList.contains("sidebar-mini"));
      } else {
        body.classList.toggle("sidebar-open");
      }

      setToggleExpanded();
    }

    function addCloseHandlers(items) {
      Array.prototype.forEach.call(items, function (item) {
        item.addEventListener("click", function () {
          if (!isDesktop()) {
            closeMobileSidebar();
          }
        });
      });
    }

    if (getSavedMiniState(storageAvailable) && isDesktop()) {
      body.classList.add("sidebar-mini");
    }

    sidebarToggle.addEventListener("click", toggleSidebar);
    addCloseHandlers(closeButtons);
    addCloseHandlers(sidebarLinks);
    setToggleExpanded();

    function handleBreakpointChange() {
      if (isDesktop()) {
        body.classList.remove("sidebar-open");
        setClass(body, "sidebar-mini", getSavedMiniState(storageAvailable));
      } else {
        body.classList.remove("sidebar-mini");
      }

      setToggleExpanded();
    }

    if (mediaQuery.addEventListener) {
      mediaQuery.addEventListener("change", handleBreakpointChange);
    } else if (mediaQuery.addListener) {
      mediaQuery.addListener(handleBreakpointChange);
    }
  });
})();

(function () {
  "use strict";

  var celebrationColors = ["#2563eb", "#22c55e", "#facc15", "#ef4444", "#f97316", "#7c3aed"];

  window.launchApprovedVacationConfetti = function (celebrationKey) {
    if (!celebrationKey) {
      return;
    }

    var storageKey = "erp-rh-vacation-celebrated-v1-" + celebrationKey;
    try {
      if (window.localStorage.getItem(storageKey)) {
        return;
      }
      window.localStorage.setItem(storageKey, "shown");
    } catch (error) {
      // The animation still works when browser storage is unavailable.
    }

    var canvas = document.createElement("canvas");
    canvas.className = "vacation-celebration-canvas";
    canvas.setAttribute("aria-hidden", "true");
    canvas.style.position = "fixed";
    canvas.style.top = "0";
    canvas.style.left = "0";
    canvas.style.width = "100%";
    canvas.style.height = "100%";
    canvas.style.zIndex = "99999";
    canvas.style.pointerEvents = "none";
    document.body.appendChild(canvas);

    var context = canvas.getContext("2d");
    var particles = [];
    var startTime = Date.now();
    var duration = 2600;

    function resizeCelebration() {
      canvas.width = window.innerWidth;
      canvas.height = window.innerHeight;
    }

    function createCelebrationParticle(originX) {
      return {
        x: originX,
        y: -20 - Math.random() * 100,
        width: 6 + Math.random() * 7,
        height: 9 + Math.random() * 10,
        velocityX: (Math.random() - 0.5) * 5,
        velocityY: 2.5 + Math.random() * 4,
        rotation: Math.random() * Math.PI,
        rotationSpeed: (Math.random() - 0.5) * 0.25,
        color: celebrationColors[Math.floor(Math.random() * celebrationColors.length)]
      };
    }

    function drawCelebration() {
      var elapsed = Date.now() - startTime;
      context.clearRect(0, 0, canvas.width, canvas.height);

      particles.forEach(function (particle) {
        particle.x += particle.velocityX;
        particle.y += particle.velocityY;
        particle.velocityY += 0.035;
        particle.rotation += particle.rotationSpeed;
        context.save();
        context.translate(particle.x, particle.y);
        context.rotate(particle.rotation);
        context.fillStyle = particle.color;
        context.fillRect(-particle.width / 2, -particle.height / 2, particle.width, particle.height);
        context.restore();
      });

      particles = particles.filter(function (particle) {
        return particle.y < canvas.height + 30;
      });

      if (elapsed < duration || particles.length > 0) {
        window.requestAnimationFrame(drawCelebration);
      } else {
        window.removeEventListener("resize", resizeCelebration);
        canvas.remove();
      }
    }

    resizeCelebration();
    for (var i = 0; i < 150; i += 1) {
      particles.push(createCelebrationParticle(Math.random() * canvas.width));
    }
    window.addEventListener("resize", resizeCelebration);
    window.requestAnimationFrame(drawCelebration);
  };
})();
