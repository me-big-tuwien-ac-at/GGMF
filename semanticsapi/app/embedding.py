from transformers import BertTokenizer, BertModel
from torch import torch


class Embedding:

    # TODO: remove
    explanations = {
"Order": "A node in a knowledge graph representing a request placed by a customer to purchase one or more products or services from a business.",
"Account": "A node in a knowledge graph representing a customer's account that holds information and records related to their interactions and purchases.",
"Invoice": "A node in a knowledge graph representing a document issued by a seller to a buyer, itemizing the products or services provided and their costs.",
"Customer": "A node in a knowledge graph representing an individual or organization that engages in transactions or purchases products or services from a business.",
"packagedElement": "A node in a knowledge graph representing a packaged element, such as a class, component, or any other software artifact or entity that can be included in a package.",
"ShippingDetails": "A node in a knowledge graph representing the information related to the shipment and delivery of a product from a seller to a buyer.",
"PaymentManagement": "A node in a knowledge graph representing the system or process responsible for managing and handling payments for products or services.",
"LineItem": "A node in a knowledge graph representing an individual item or product within an order or invoice, specifying its quantity, price, and other relevant details.",
"Product": "A node in a knowledge graph representing a tangible or intangible item that is offered for sale by a business.",
"PaymentTransaction": "A node in a knowledge graph representing a specific instance of a payment made by a customer for a product or service.",
"Payment": "A node in a knowledge graph representing the transfer of funds or value from a customer to a seller as a form of compensation for a product or service.",
"ShoppingCart": "A node in a knowledge graph representing a virtual cart or basket where customers can add and manage the products they wish to purchase.",
"WebUser": "A node in a knowledge graph representing an individual who interacts with a website or web application, typically as a registered or logged-in user.",
"ownedAttribute": "A node in a knowledge graph representing an attribute that is owned by a class or other entity, indicating a characteristic or property associated with that entity.",
"ProductManagement": "A node in a knowledge graph representing the process or system involved in managing the lifecycle of products, including their creation, pricing, and availability.",
"LogisticManagement": "A node in a knowledge graph representing the process or system responsible for managing and coordinating the movement of products from the seller to the buyer, including shipping, storage, and distribution."
}

    def __init__(self):
        self.tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
        self.model = BertModel.from_pretrained('bert-base-uncased')

    def determine_embedding(self, text):

        tokenised_text = self.tokenizer(self.explanations[text], return_tensors="pt")

        #tokenised_text = self.tokenizer(text, return_tensors="pt")

        # Retrieve the hidden states and calculate the means of the layer to get
        output = self.model(**tokenised_text, output_hidden_states=True)
        hidden_states = output.hidden_states
        token_vecs = hidden_states[-2][0]

        # Calculate the means of the layer to get the embedding
        return torch.mean(token_vecs, dim=0).tolist()
