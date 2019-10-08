inputDims=(imageSize,imageSize,3)

#load this architecture first...
def create_model():
    model = Sequential()
    
    model.add(Conv2D(128,(3,3),input_shape=(200,200,3)))
    model.add(LeakyReLU(alpha=0.05))
    model.add(MaxPooling2D(pool_size=(2,2)))
    model.add(Dropout(0.1))
    
    model.add(Conv2D(64,(3,3),input_shape=(200,200,3)))
    model.add(LeakyReLU(alpha=0.05))
    #model.add(MaxPooling2D(pool_size=(2,2)))
    model.add(Dropout(0.1))
    
    model.add(Conv2D(128,kernel_size=(3,3),strides=(2,2),input_shape=(200,200,3)))
    model.add(LeakyReLU(alpha=0.05))
    model.add(MaxPooling2D(pool_size=(3,3)))
    model.add(Dropout(0.3))

    model.add(Flatten())
    
    #model.add(Dense(128))
    #model.add(Activation("relu"))
    #model.add(Dropout(0.3))
    
    model.add(Dense(32))
    model.add(Activation("relu"))
    model.add(Dropout(0.2))
    
    model.add(Dense(29))
    model.add(Activation("softmax"))
    
    return model

def train_model(model):
    #import this from tf.losses...
    model.compile(loss=categorical_cross_entropy, metrics=['accuracy'], optimizer='Adadelta')
    checkpointer = ModelCheckpoint(filepath="/tmp/weights2.hdf5", verbose=1, save_best_only=True)
    #log_dir="logs/fit/" + datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    #tensorboard_callback = tf.keras.callbacks.TensorBoard(log_dir=log_dir, histogram_freq=1)
    model.fit_generator(train,epochs=10,validation_data=test)
    
def save_trained_model(model):
    model.save("model-3Conv-2Dense-10Epoch.h5")

def load_trained_model(weights_path):
    model = create_model()
    model.load_weights(weights_path)
    return model