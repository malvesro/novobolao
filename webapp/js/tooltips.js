(function () {
	const TOOLTIP_ATTR = "data-tooltip";
	const TOOLTIP_ID = "app-tooltip";
	const GAP = 12;

	let tooltipElement = null;
	let hideTimeout = null;
	let activeTrigger = null;

	function ensureTooltipElement() {
		if (!tooltipElement) {
			const element = document.createElement("div");
			element.id = TOOLTIP_ID;
			element.className = "tooltip";
			element.setAttribute("role", "tooltip");
			element.setAttribute("aria-hidden", "true");
			document.body.appendChild(element);
			tooltipElement = element;
		}
		return tooltipElement;
	}

	function scheduleHide(immediate) {
		const tooltip = ensureTooltipElement();
		if (hideTimeout) {
			clearTimeout(hideTimeout);
			hideTimeout = null;
		}

		const hideFn = function () {
			if (tooltip) {
				tooltip.classList.remove("tooltip--visible");
				tooltip.classList.remove("tooltip--below");
				tooltip.setAttribute("aria-hidden", "true");
				tooltip.style.left = "-9999px";
				tooltip.style.top = "-9999px";
			}
			if (activeTrigger) {
				activeTrigger = null;
			}
		};

		if (immediate) {
			hideFn();
			return;
		}

		hideTimeout = window.setTimeout(hideFn, 120);
	}

	function positionTooltip(trigger, tooltip) {
		const rect = trigger.getBoundingClientRect();
		const tooltipRect = tooltip.getBoundingClientRect();
		const viewportWidth = window.innerWidth;
		const viewportHeight = window.innerHeight;

		let top = rect.top - tooltipRect.height - GAP;
		let left = rect.left + (rect.width - tooltipRect.width) / 2;
		let placeBelow = false;

		const clampedLeft = Math.max(8, Math.min(left, viewportWidth - tooltipRect.width - 8));
		left = clampedLeft;

		if (top < 8 || rect.top < tooltipRect.height + GAP) {
			top = rect.bottom + GAP;
			placeBelow = true;
		}

		if (top + tooltipRect.height > viewportHeight - 8) {
			top = Math.max(8, viewportHeight - tooltipRect.height - 8);
		}

		tooltip.style.left = `${Math.round(left)}px`;
		tooltip.style.top = `${Math.round(top)}px`;
		if (placeBelow) {
			tooltip.classList.add("tooltip--below");
		} else {
			tooltip.classList.remove("tooltip--below");
		}
	}

	function showTooltip(trigger) {
		const tooltipText = trigger.getAttribute(TOOLTIP_ATTR);
		if (!tooltipText) {
			return;
		}

		const tooltip = ensureTooltipElement();

		if (hideTimeout) {
			clearTimeout(hideTimeout);
			hideTimeout = null;
		}

		tooltip.textContent = tooltipText;
		tooltip.setAttribute("aria-hidden", "false");
		tooltip.classList.add("tooltip--visible");

		// Ensure element is sized before positioning.
		tooltip.style.left = "-9999px";
		tooltip.style.top = "-9999px";
		// Force reflow to capture latest size.
		tooltip.getBoundingClientRect();
		positionTooltip(trigger, tooltip);
		activeTrigger = trigger;
		trigger.setAttribute("aria-describedby", TOOLTIP_ID);
	}

	function handlePointerEnter(event) {
		showTooltip(event.currentTarget);
	}

	function handlePointerLeave() {
		scheduleHide(false);
	}

	function handleFocus(event) {
		showTooltip(event.currentTarget);
	}

	function handleBlur() {
		scheduleHide(false);
	}

	function handleKeyDown(event) {
		if (event.key === "Escape" || event.key === "Esc") {
			scheduleHide(true);
		}
	}

	function bindTooltip(element) {
		if (element.dataset.tooltipBound === "true") {
			return;
		}

		let tooltipText = element.getAttribute(TOOLTIP_ATTR);
		if (!tooltipText) {
			const legacyTitle = element.getAttribute("title");
			if (legacyTitle) {
				tooltipText = legacyTitle;
				element.setAttribute(TOOLTIP_ATTR, legacyTitle);
				element.removeAttribute("title");
			}
		}

		if (!tooltipText) {
			return;
		}

		element.dataset.tooltipBound = "true";
		element.addEventListener("mouseenter", handlePointerEnter);
		element.addEventListener("mouseleave", handlePointerLeave);
		element.addEventListener("focus", handleFocus);
		element.addEventListener("blur", handleBlur);
		element.addEventListener("keydown", handleKeyDown);
	}

	function initialiseTooltips(root) {
		const scope = root && root.querySelectorAll ? root : document;
		const elements = scope.querySelectorAll(`[${TOOLTIP_ATTR}]`);
		elements.forEach(bindTooltip);
	}

	document.addEventListener("DOMContentLoaded", function () {
		initialiseTooltips(document);
	});

	document.body.addEventListener("htmx:afterSwap", function (event) {
		if (!event.target) {
			return;
		}
		initialiseTooltips(event.target);
	});

	window.addEventListener("scroll", function () {
		if (activeTrigger && tooltipElement && tooltipElement.classList.contains("tooltip--visible")) {
			positionTooltip(activeTrigger, tooltipElement);
		}
	});

	window.addEventListener("resize", function () {
		if (activeTrigger && tooltipElement && tooltipElement.classList.contains("tooltip--visible")) {
			positionTooltip(activeTrigger, tooltipElement);
		}
	});
})();
