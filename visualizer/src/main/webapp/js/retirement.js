function displayDetails(event) {
    children = document.getElementsByClassName(event.currentTarget.id);
    for (let child of children) {
        if (child.style.visibility == "visible")
            child.style.visibility = "collapse";
        else
            child.style.visibility = "visible";
    }
}
window.addEventListener("DOMContentLoaded", function() {
    var subTotals = document.getElementsByClassName("subtotal");
    for (let subTotal of subTotals) {
        subTotal.onclick = displayDetails;
    }
}, false);
